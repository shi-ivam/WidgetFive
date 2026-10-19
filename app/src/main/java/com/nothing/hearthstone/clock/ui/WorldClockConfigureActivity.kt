package com.nothing.hearthstone.clock.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nothing.hearthstone.R
import com.nothing.hearthstone.clock.provider.WorldWidgetProvider

class WorldClockConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analog_clock_configure)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        try { findViewById<android.widget.TextView>(R.id.setting_title).text = getString(R.string.world_clock_config_title) } catch (_: Exception) { }

        val prefs = getSharedPreferences(WorldWidgetProvider.PREFS, MODE_PRIVATE)

        findViewById<androidx.cardview.widget.CardView>(R.id.analog_clock_style_bold).setOnClickListener {
            prefs.edit().putBoolean(WorldWidgetProvider.simpleKey(appWidgetId), false).apply()
            finishOk()
        }
        findViewById<androidx.cardview.widget.CardView>(R.id.analog_clock_style_scale).setOnClickListener {
            prefs.edit().putBoolean(WorldWidgetProvider.simpleKey(appWidgetId), true).apply()
            finishOk()
        }
    }

    private fun finishOk() {
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}