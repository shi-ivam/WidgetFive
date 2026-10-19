package com.nothing.hearthstone.chatgpt.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.nothing.hearthstone.R
import com.nothing.hearthstone.chatgpt.ui.GPTScrollableConfigureActivity

class GPTScrollableProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_REFRESH -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, GPTScrollableProvider::class.java)
                val ids = mgr.getAppWidgetIds(cn)
                for (id in ids) update(context, mgr, id)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            prefs.remove(aodKey(id))
        }
        prefs.apply()
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_chatgpt_2x2_scrollable)

        // QoL: tap opens ChatGPT (web) for prompt/voice query
        val chatIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.openai.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val chatPi = android.app.PendingIntent.getActivity(
            context, appWidgetId, chatIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        // QoL: voice query launcher via speech recognizer, falling back to ChatGPT web
        val voiceIntent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Ask ChatGPT")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val voicePi = try {
            android.app.PendingIntent.getActivity(
                context, appWidgetId + 1000, voiceIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        } catch (_: Exception) { chatPi }

        val clickTargets = listOf("chatgpt_container", "chatgpt_viewpager", "chatgpt_page_indicator")
        for (name in clickTargets) {
            val id = context.resources.getIdentifier(name, "id", context.packageName)
            if (id != 0) {
                try { views.setOnClickPendingIntent(id, chatPi) } catch (_: Exception) { }
            }
        }
        try { views.setOnClickPendingIntent(R.id.chatgpt_container, chatPi) } catch (_: Exception) { }

        // Configure entry point
        val cfgIntent = Intent(context, GPTScrollableConfigureActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val cfgPi = android.app.PendingIntent.getActivity(
            context, appWidgetId + 2000, cfgIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.chatgpt_page_indicator, cfgPi) } catch (_: Exception) { }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "gpt_scrollable_prefs"
        fun aodKey(id: Int) = "aod_$id"
        const val ACTION_REFRESH = "com.nothing.hearthstone.chatgpt.scrollable.ACTION_REFRESH"
    }
}