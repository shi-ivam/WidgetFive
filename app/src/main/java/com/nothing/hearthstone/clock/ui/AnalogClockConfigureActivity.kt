package com.nothing.hearthstone.clock.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.nothing.hearthstone.R
import com.nothing.hearthstone.clock.provider.AnalogWidgetProvider

class AnalogClockConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analog_clock_configure)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val prefs = getSharedPreferences(AnalogWidgetProvider.PREFS, MODE_PRIVATE)

        findViewById<androidx.cardview.widget.CardView>(R.id.analog_clock_style_bold).setOnClickListener {
            prefs.edit().putString(AnalogWidgetProvider.styleKey(appWidgetId), "bold").apply()
            finishOk()
        }
        findViewById<androidx.cardview.widget.CardView>(R.id.analog_clock_style_scale).setOnClickListener {
            prefs.edit().putString(AnalogWidgetProvider.styleKey(appWidgetId), "scale").apply()
            finishOk()
        }
    }

    private fun finishOk() {
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(Activity.RESULT_OK, result)
        finish()
    }
}
