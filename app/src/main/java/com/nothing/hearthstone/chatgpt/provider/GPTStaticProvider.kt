package com.nothing.hearthstone.chatgpt.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class GPTStaticProvider : AppWidgetProvider() {

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
                val cn = android.content.ComponentName(context, GPTStaticProvider::class.java)
                val ids = mgr.getAppWidgetIds(cn)
                for (id in ids) update(context, mgr, id)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            prefs.remove(typeKey(id))
        }
        prefs.apply()
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val type = prefs.getString(typeKey(appWidgetId), "voice") ?: "voice"
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val large = minW >= 110 && minH >= 110
        val wide = minW >= 160 && minH < 110
        val layout = when {
            large -> R.layout.widget_chatgpt_2x2
            wide -> R.layout.widget_chatgpt_1x2
            else -> R.layout.widget_chatgpt_1x1
        }
        val views = RemoteViews(context.packageName, layout)
        val icon = when (type) {
            "text" -> R.drawable.ic_chatgpt_text
            "image" -> R.drawable.ic_chatgpt_image
            else -> R.drawable.ic_chatgpt_voice
        }
        views.setImageViewResource(R.id.iv_chatgpt, icon)
        if (layout == R.layout.widget_chatgpt_1x1) {
            try { views.setImageViewResource(R.id.bg_chatgpt, R.drawable.chatgpt_1x1_bg) } catch (_: Exception) { }
        }

        val mode = when (type) {
            "text" -> "text"
            "image" -> "camera"
            else -> "voice"
        }
        val uri = "https://chat.openai.com/?mode=$mode"
        val chatIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage("com.openai.chatgpt")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val resolvedIntent = if (appIntent.resolveActivity(context.packageManager) != null) appIntent else chatIntent
        val chatPi = android.app.PendingIntent.getActivity(
            context, appWidgetId, resolvedIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

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

        val clickTargets = listOf("container_chatgpt", "iv_chatgpt", "bg_chatgpt", "tv_chatgpt")
        for (name in clickTargets) {
            val id = context.resources.getIdentifier(name, "id", context.packageName)
            if (id != 0) {
                try { views.setOnClickPendingIntent(id, if (type == "voice") voicePi else chatPi) } catch (_: Exception) { }
            }
        }
        try { views.setOnClickPendingIntent(R.id.container_chatgpt, if (type == "voice") voicePi else chatPi) } catch (_: Exception) { }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "gpt_static_prefs"
        fun typeKey(id: Int) = "type_$id"
        const val ACTION_REFRESH = "com.nothing.hearthstone.chatgpt.static.ACTION_REFRESH"
    }
}