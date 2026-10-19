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
            views.setImageViewResource(R.id.bg_chatgpt, R.drawable.chatgpt_1x1_bg)
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.openai.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.container_chatgpt, pi)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "gpt_static_prefs"
        fun typeKey(id: Int) = "type_$id"
    }
}