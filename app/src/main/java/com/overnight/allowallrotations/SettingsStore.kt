package com.overnight.allowallrotations

import android.content.Context
import io.github.libxposed.service.XposedService

/**
 * 設定アプリ専用: 設定のローカル保存と、LSPosed フレームワークへの
 * 同期 (Remote Preferences)。フック側プロセスからは参照しないこと
 * (io.github.libxposed.service は設定アプリ側でのみ使う)。
 */
object SettingsStore {

    private const val LOCAL_PREFS = "local_settings"

    @Volatile
    private var service: XposedService? = null

    fun bind(svc: XposedService) {
        service = svc
    }

    fun unbind(svc: XposedService) {
        if (service == svc) service = null
    }

    fun isConnected(): Boolean = service != null

    fun getAllow(context: Context): Boolean =
        context.getSharedPreferences(LOCAL_PREFS, Context.MODE_PRIVATE)
            .getBoolean(Keys.ALLOW_ALL_ROTATIONS, true)

    fun setAllow(context: Context, value: Boolean) {
        context.getSharedPreferences(LOCAL_PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(Keys.ALLOW_ALL_ROTATIONS, value).apply()
        syncToRemote(context)
    }

    /**
     * ローカル設定を Remote Preferences へコピーする。
     * 未接続なら何もしない (次回接続時に App が再実行する)。
     */
    fun syncToRemote(context: Context) {
        val svc = service ?: return
        try {
            svc.getRemotePreferences(Keys.GROUP).edit()
                .putBoolean(Keys.ALLOW_ALL_ROTATIONS, getAllow(context))
                .apply()
        } catch (t: Throwable) {
            // フレームワーク未有効化など。ローカルには保存済みなので
            // 次回接続時の同期で回復する
        }
    }
}
