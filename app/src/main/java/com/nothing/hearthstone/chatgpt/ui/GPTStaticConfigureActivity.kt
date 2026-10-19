package com.nothing.hearthstone.chatgpt.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nothing.hearthstone.R
import com.nothing.hearthstone.chatgpt.provider.GPTStaticProvider

class GPTStaticConfigureActivity : AppCompatActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }
        setContentView(R.layout.activity_gpt_static_configure)
        val prefs = getSharedPreferences(GPTStaticProvider.PREFS, Context.MODE_PRIVATE)
        findViewById<android.view.View>(R.id.chatgpt_type_voice).setOnClickListener {
            saveAndFinish(prefs, "voice")
        }
        findViewById<android.view.View>(R.id.chatgpt_type_text).setOnClickListener {
            saveAndFinish(prefs, "text")
        }
        findViewById<android.view.View>(R.id.chatgpt_type_image).setOnClickListener {
            saveAndFinish(prefs, "image")
        }
    }

    private fun saveAndFinish(prefs: android.content.SharedPreferences, type: String) {
        prefs.edit().putString(GPTStaticProvider.typeKey(appWidgetId), type).apply()
        val awm = AppWidgetManager.getInstance(this)
        GPTStaticProvider().onUpdate(this, awm, intArrayOf(appWidgetId))
        setResult(Activity.RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        finish()
    }
}