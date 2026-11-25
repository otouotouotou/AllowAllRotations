package com.overnight.allowallrotations

/**
 * 設定キー定義。設定アプリ側とフック側の両方から参照する。
 * libxposed のクラスには一切触れないこと (フック側は compileOnly の
 * API しか持たず、設定アプリ側は service しか持たないため)。
 */
object Keys {
    /** Remote Preferences のグループ名 */
    const val GROUP = "settings"

    /** 「すべての回転を許可」 (true で 180° を含む全回転を許可) */
    const val ALLOW_ALL_ROTATIONS = "allow_all_rotations"
}
