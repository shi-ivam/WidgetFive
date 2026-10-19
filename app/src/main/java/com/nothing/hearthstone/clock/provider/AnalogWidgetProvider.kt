package com.nothing.hearthstone.clock.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class AnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val style = prefs.getString(styleKey(appWidgetId), "bold") ?: "bold"
        val simple = prefs.getBoolean(simpleKey(appWidgetId), false)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val large = minW >= 110 && minH >= 110
        val layout = when {
            style == "scale" -> if (large) {
                if (simple) R.layout.card_analog_clock_scale_2x2_simple else R.layout.card_analog_clock_scale_2x2
            } else {
                if (simple) R.layout.card_analog_clock_scale_1x1_simple else R.layout.card_analog_clock_scale_1x1
            }
            else -> if (large) {
                if (simple) R.layout.card_analog_clock_bold_2x2_simple else R.layout.card_analog_clock_bold_2x2
            } else {
                if (simple) R.layout.card_analog_clock_bold_1x1_simple else R.layout.card_analog_clock_bold_1x1
            }
        }
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(android.R.id.background, pi)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "analog_clock_prefs"
        fun styleKey(id: Int) = "style_$id"
        fun simpleKey(id: Int) = "simple_$id"
    }
}
