package com.overnight.allowallrotations

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

/**
 * 設定画面: スイッチ 1 個だけ。
 * 「すべての回転を許可」を ON にすると、センサー回転で 180°
 * (逆向き) も候補に入る。反映には端末の再起動が必要
 * (system_server のコンストラクタフックのため)。
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = dp(24)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            // ステータスバーと重ならないようインセット分を空ける
            setOnApplyWindowInsetsListener { v, insets ->
                val bars = insets.systemWindowInsetTop
                v.setPadding(pad, pad + bars, pad, pad)
                insets
            }
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
        }
        root.addView(title)

        val desc = TextView(this).apply {
            text = getString(R.string.app_description)
            textSize = 14f
            setPadding(0, dp(8), 0, dp(24))
        }
        root.addView(desc)

        // --- スイッチ行 ---
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val labels = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
        }
        val switchLabel = TextView(this).apply {
            text = getString(R.string.allow_all_rotations)
            textSize = 16f
        }
        val switchSummary = TextView(this).apply {
            text = getString(R.string.allow_all_rotations_summary)
            textSize = 13f
            setPadding(0, dp(4), dp(12), 0)
        }
        labels.addView(switchLabel)
        labels.addView(switchSummary)
        row.addView(labels)

        val toggle = Switch(this).apply {
            isChecked = SettingsStore.getAllow(this@MainActivity)
            setOnCheckedChangeListener { _, checked ->
                SettingsStore.setAllow(this@MainActivity, checked)
            }
        }
        row.addView(toggle)
        root.addView(row)

        val note = TextView(this).apply {
            text = getString(R.string.note_reboot)
            textSize = 13f
            setPadding(0, dp(24), 0, 0)
        }
        root.addView(note)

        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        scroll.requestApplyInsets()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
