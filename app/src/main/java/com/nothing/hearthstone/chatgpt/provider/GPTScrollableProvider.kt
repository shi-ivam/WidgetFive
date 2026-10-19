package com.nothing.hearthstone.chatgpt.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
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

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_chatgpt_2x2_scrollable)
        val intent = Intent(context, GPTScrollableConfigureActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.chatgpt_container, pi)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "gpt_scrollable_prefs"
        fun aodKey(id: Int) = "aod_$id"
    }
}