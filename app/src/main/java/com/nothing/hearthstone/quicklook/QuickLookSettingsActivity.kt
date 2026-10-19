package com.nothing.hearthstone.quicklook

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nothing.hearthstone.R

class QuickLookSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            // default: non-AOD (normal) mode
            getSharedPreferences(QLTransparentWidgetProvider.PREFS, MODE_PRIVATE)
                .edit().putBoolean(QLTransparentWidgetProvider.aodKey(appWidgetId), false).apply()
            getSharedPreferences(QLCardModeWidgetProvider.PREFS, MODE_PRIVATE)
                .edit().putBoolean(QLCardModeWidgetProvider.aodKey(appWidgetId), false).apply()
        }
        setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
}