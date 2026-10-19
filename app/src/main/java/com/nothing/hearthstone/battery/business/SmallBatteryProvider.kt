package com.nothing.hearthstone.battery.business

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class SmallBatteryProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val aod = prefs.getBoolean(aodKey(appWidgetId), false)
        val layout = if (aod) R.layout.widget_battery_1x1_aod else R.layout.battery_small_container
        val views = RemoteViews(context.packageName, layout)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "small_battery_prefs"
        fun aodKey(id: Int) = "aod_$id"
    }
}