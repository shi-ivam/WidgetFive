package com.nothing.hearthstone.clock.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class WorldWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val simple = prefs.getBoolean(simpleKey(appWidgetId), false)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val wide = minW >= 110 && minH < 110
        val tall = minH >= 110 && minW < 110
        val large = minW >= 110 && minH >= 110
        val layout = when {
            simple && large -> R.layout.card_world_clock_2x2_simple
            simple && wide -> R.layout.card_world_clock_1x2_simple
            simple && tall -> R.layout.card_world_clock_2x1_simple
            simple -> R.layout.card_world_clock_1x1_simple
            large -> R.layout.card_world_clock_2x2
            wide -> R.layout.card_world_clock_1x2
            tall -> R.layout.card_world_clock_2x1
            else -> R.layout.card_world_clock_1x1
        }
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.container, pi)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "world_clock_prefs"
        fun simpleKey(id: Int) = "simple_$id"
    }
}