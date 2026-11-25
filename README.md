# AllowAllRotations

全ての回転方向を許可するだけのLSPosedモジュール

センサー回転 (自動回転 / 縦センサー / 横センサー)で180°(逆向き)が
選ばれるかどうかは、端末の`allowAllRotations`
(`config_allowAllRotations` / `DisplayRotation`)で決まります。
HyperOS3はこれを無効にしているため、Asrishで縦センサーを設定しても逆さに反転しません。
本モジュールはそれを回避します

## 検証環境 / 注意

**検証済みなのは HyperOS 3 (Android 16、Xiaomi 17 Ultra) のみです。**
仕組み自体はAOSP 標準のものなので他の ROM でも動くかもしれませんが、
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

---

# AllowAllRotations (English)

An LSPosed module that simply allows all rotation directions.

Whether sensor rotation (auto-rotate / sensor portrait / sensor landscape)
can select 180° (upside down) is decided by the device's `allowAllRotations`
(`config_allowAllRotations` / `DisplayRotation`).
HyperOS 3 has this disabled, so even if you set sensor portrait in Asrish,
the screen never flips upside down. This module works around that.

## Verified environment / Note

**Verified only on HyperOS 3 (Android 16, Xiaomi 17 Ultra).**
The mechanism itself is standard AOSP, so it may work on other ROMs,
but **there is no guarantee**.

## How it works

Scope is `system` (system framework) only.

- Hooks `DisplayRotation.getAllowAllRotations()` to always return
  enabled (1) while the setting is ON (the main hook)
- Writes `mAllowAllRotations` right after the `DisplayRotation`
  constructor (fallback)
- `RotationPolicy.areAllRotationsAllowed` returns the setting value

One switch (default ON). Settings are passed via LSPosed Remote
Preferences. A reboot is required to apply changes.

## Requirements

- LSPosed (Modern Xposed API 102)
- APK minSdk 27 (Android 8.1)

## License

Apache License 2.0
