package com.nothing.hearthstone.clock.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class DigitalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_TIME_CHANGED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, DigitalWidgetProvider::class.java))
            onUpdate(context, manager, ids)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) prefs.remove(simpleKey(id))
        prefs.apply()
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val simple = prefs.getBoolean(simpleKey(appWidgetId), false)
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val wide = minW >= 110 && minH < 110   // 1x2 / 2x1 landscape-ish
        val tall = minH >= 110 && minW < 110     // 2x1 vertical
        val layout = when {
            simple && wide -> R.layout.card_digital_clock_simple_1_by_2
            simple && tall -> R.layout.card_digital_clock_simple_2_by_1
            simple -> R.layout.card_digital_clock_simple_1_by_1
            wide -> R.layout.card_digital_clock_1_by_2
            tall -> R.layout.card_digital_clock_2_by_1
            else -> R.layout.card_digital_clock_1_by_1
        }
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.container, pi)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "digital_clock_prefs"
        fun simpleKey(id: Int) = "simple_$id"
    }
}
