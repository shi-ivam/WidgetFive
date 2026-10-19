package com.nothing.hearthstone.screentime.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R
import com.nothing.hearthstone.screentime.ui.ScreenTimeSettingActivity

class MiddleShapeScreenTimeProvider : AppWidgetProvider() {

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
            Intent.ACTION_TIMEZONE_CHANGED -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, MiddleShapeScreenTimeProvider::class.java)
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
        val layout = if (aod) R.layout.widget_screen_aod_time_shape else R.layout.widget_screen_time_shape
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(context, ScreenTimeSettingActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.fl_shape_container, pi)
        // QoL: make the whole shape surface tappable to configure
        try { views.setOnClickPendingIntent(R.id.ppv, pi) } catch (_: Exception) { }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "screen_time_shape_prefs"
        fun aodKey(id: Int) = "aod_$id"
    }
}