package com.overnight.allowallrotations

import android.content.Context
import android.util.Log
import io.github.libxposed.api.XposedInterface.ExceptionMode
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import java.util.concurrent.atomic.AtomicInteger

/**
 * AllowAllRotations — 「すべての回転を許可」だけを行うモジュール。
 *
 * センサー回転 (縦センサー等) で 180° が候補に入るかは、フレームワークの
 * DisplayRotation が握っている。Android 16 の実装では:
 *
 *  - mAllowAllRotations はコンストラクタでは決まらず、初回利用時に
 *    getAllowAllRotations() が config_allowAllRotations リソースを
 *    読んで遅延初期化する (UNDEFINED=-1 → ENABLED=1 / DISABLED=0)
 *  - resetAllowAllRotations() が呼ばれると UNDEFINED に戻り、
 *    次回利用時にリソースを読み直す
 *    (DisplayWindowSettings.applyRotationSettingsToDisplayLocked から
 *    呼ばれる。表示設定の適用タイミングで発生しうる)
 *
 * そのためコンストラクタ直後のフィールド書き換えだけでは、リセットが
 * 入るとリソース (多くの ROM では false) に負ける。本モジュールは
 * system_server (スコープ: android) で次の 3 点を行う:
 *
 *  1. getAllowAllRotations() をフックし、設定 ON の間は常に
 *     ENABLED (1) を返す (遅延初期化とリセットの両方に勝つ本命)
 *  2. DisplayRotation のコンストラクタ直後に mAllowAllRotations を
 *     設定値へ書き換える (フィールドを直接読む経路への保険)
 *  3. RotationPolicy.areAllRotationsAllowed(Context) を設定値で返す
 *     (SystemUI 等の RotationPolicy 経由の判定に効く)
 *
 * 設定の受け渡しは Remote Preferences (LSPosed フレームワーク経由)。
 * system_server 起動時に 1 回だけ読む。設定変更の反映には再起動が必要。
 *
 * 診断ログは XposedModule.log() 経由で出す (LSPosed のモジュール
 * ログに確実に残る経路。android.util.Log は起動直後の取りこぼしが
 * v0.1.1 で確認されたため使わない)。
 */
class MainHook : XposedModule() {

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        log(Log.INFO, TAG, "onSystemServerStarting")
        val allow = try {
            getRemotePreferences(Keys.GROUP)
                .getBoolean(Keys.ALLOW_ALL_ROTATIONS, true)
        } catch (t: Throwable) {
            log(Log.WARN, TAG, "remote prefs read failed, defaulting to true", t)
            true
        }
        log(Log.INFO, TAG, "allow=$allow")
        val cl = param.classLoader

        try {
            val drClass = Class.forName(
                "com.android.server.wm.DisplayRotation", false, cl
            )
            val field = drClass.getDeclaredField("mAllowAllRotations")
            field.isAccessible = true
            log(
                Log.INFO, TAG,
                "DisplayRotation found, field type=${field.type.name}, " +
                    "constructors=${drClass.declaredConstructors.size}"
            )

            // --- 1. getAllowAllRotations() (本命) ---
            try {
                val getter = drClass.getDeclaredMethod("getAllowAllRotations")
                hook(getter)
                    .setId("allowallrotations.getAllowAllRotations")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept { chain ->
                        if (allow) {
                            val n = getterCalls.incrementAndGet()
                            if (n <= 5) {
                                log(
                                    Log.INFO, TAG,
                                    "getAllowAllRotations -> 1 (call #$n)"
                                )
                            }
                            1
                        } else {
                            chain.proceed()
                        }
                    }
                log(Log.INFO, TAG, "getAllowAllRotations hook installed")
            } catch (t: Throwable) {
                log(Log.WARN, TAG, "getAllowAllRotations not found", t)
            }

            // --- 2. コンストラクタ直後のフィールド書き換え (保険) ---
            for (ctor in drClass.declaredConstructors) {
                hook(ctor)
                    .setId("allowallrotations.displayRotation")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept { chain ->
                        val result = chain.proceed()
                        try {
                            val before = field.getInt(chain.thisObject)
                            field.setInt(
                                chain.thisObject, if (allow) 1 else 0
                            )
                            log(
                                Log.INFO, TAG,
                                "DisplayRotation created: " +
                                    "mAllowAllRotations $before -> " +
                                    if (allow) 1 else 0
                            )
                        } catch (t: Throwable) {
                            log(Log.WARN, TAG, "field set failed", t)
                        }
                        result
                    }
            }
            log(Log.INFO, TAG, "constructor hooks installed")
        } catch (t: Throwable) {
            log(Log.WARN, TAG, "DisplayRotation hook failed", t)
        }

        // --- 3. RotationPolicy.areAllRotationsAllowed ---
        try {
            val rpClass = Class.forName(
                "com.android.internal.view.RotationPolicy", false, cl
            )
            val method = rpClass.getDeclaredMethod(
                "areAllRotationsAllowed", Context::class.java
            )
            hook(method)
                .setId("allowallrotations.rotationPolicy")
                .setExceptionMode(ExceptionMode.PROTECTIVE)
                .intercept { chain ->
                    if (allow) true else chain.proceed()
                }
            log(Log.INFO, TAG, "RotationPolicy hook installed")
        } catch (t: Throwable) {
            log(Log.WARN, TAG, "RotationPolicy hook failed", t)
        }

        log(Log.INFO, TAG, "hooks installed (allow=$allow)")
    }

    private companion object {
        const val TAG = "AllowAllRotations"
        val getterCalls = AtomicInteger(0)
    }
}
