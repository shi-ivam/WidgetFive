package com.nothing.hearthstone.pedometer

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R
import com.nothing.hearthstone.pedometer.PedometerSettingsActivity

class WidgetDailyCountProvider : AppWidgetProvider() {

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
            Intent.ACTION_DATE_CHANGED -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, WidgetDailyCountProvider::class.java)
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
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val aod = prefs.getBoolean(aodKey(appWidgetId), false)
        val layout = if (aod) R.layout.widget_daily_count_card_aod else R.layout.widget_daily_count_card
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(context, PedometerSettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.daily_count_view, pi)
        // QoL: make the whole daily count surface tappable to configure
        try { views.setOnClickPendingIntent(R.id.count_content_layout, pi) } catch (_: Exception) { }
        try { views.setOnClickPendingIntent(R.id.today_steps_value_txt, pi) } catch (_: Exception) { }
        try { views.setOnClickPendingIntent(R.id.error_tv, pi) } catch (_: Exception) { }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "pedometer_daily_count_prefs"
        fun aodKey(id: Int) = "aod_$id"
    }
}