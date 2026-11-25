# AllowAllRotations

すべての回転を許可するだけの LSPosed モジュール (Modern Xposed API 102)。

センサー回転 (自動回転 / 縦センサー / 横センサー) で 180° (逆向き) が
選ばれるかどうかは、端末の `allowAllRotations`
(`config_allowAllRotations` / `DisplayRotation`) で決まります。
多くの ROM はこれを無効で出荷するため、縦センサーでも逆さに反転しません。
本モジュールは system_server でその門番を開けます。

## 検証環境 / 注意

**検証済みなのは HyperOS 3 (Android 16、Xiaomi 17 Ultra) のみです。**
仕組み自体は AOSP 標準のものなので他の ROM でも動くかもしれませんが、
**動作は保証できません。**

Verified only on HyperOS 3 (Android 16). It may work on other ROMs,
but there is no guarantee.

## 仕組み

スコープは `system` (システムフレームワーク) のみ。

- `DisplayRotation.getAllowAllRotations()` をフックし、設定 ON の間は
  常に有効 (1) を返す (本命)
- `DisplayRotation` のコンストラクタ直後に `mAllowAllRotations` を
  設定値 (1 / 0) へ書き換え (保険)
- `RotationPolicy.areAllRotationsAllowed` を設定値で返す

設定はスイッチ 1 個 (デフォルト ON)。受け渡しは LSPosed Remote
Preferences。反映には端末の再起動が必要です。

## 動作環境

- LSPosed (Modern Xposed API 102 対応)
- APK の minSdk は 27 (Android 8.1)

## ライセンス

Apache License 2.0
