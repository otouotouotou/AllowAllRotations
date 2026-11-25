package com.overnight.allowallrotations

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

/**
 * 設定アプリの Application。
 * LSPosed フレームワークのサービス接続を受け取り、SettingsStore に
 * 渡す。接続できたらローカル設定を Remote Preferences へ同期する。
 */
class App : Application(), XposedServiceHelper.OnServiceListener {

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(service: XposedService) {
        SettingsStore.bind(service)
        SettingsStore.syncToRemote(this)
    }

    override fun onServiceDied(service: XposedService) {
        SettingsStore.unbind(service)
    }
}
