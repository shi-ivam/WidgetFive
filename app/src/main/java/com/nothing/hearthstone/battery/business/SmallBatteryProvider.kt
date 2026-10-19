package com.nothing.hearthstone.battery.business

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class SmallBatteryProvider : AppWidgetProvider() {

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
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            ACTION_REFRESH -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, SmallBatteryProvider::class.java)
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
        val layout = if (aod) R.layout.widget_battery_1x1_aod else R.layout.battery_small_container
        val views = RemoteViews(context.packageName, layout)

        val level = getBatteryLevel(context)
        val charging = isCharging(context)

        if (!aod) {
            // Best-effort: clear permission overlay text
            try { views.setTextViewText(R.id.tv_battery_permission, "") } catch (_: Exception) { }
            // QoL: annotate charging state through permission text view if present
            if (charging) {
                try { views.setTextViewText(R.id.tv_battery_permission, "$level% ⚡") } catch (_: Exception) { }
            }
        }

        // QoL: tap opens system battery / power usage screen
        val settingsIntent = Intent(Intent.ACTION_POWER_USAGE_SUMMARY).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val settingsPi = android.app.PendingIntent.getActivity(
            context, appWidgetId, settingsIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val clickTargets = listOf(
            "battery_container", "battery_small_view", "battery_view",
            "layout_battery_permission", "tv_battery_permission",
            "iv_battery_permission", "iv_battery_aod_permission"
        )
        for (name in clickTargets) {
            val id = context.resources.getIdentifier(name, "id", context.packageName)
            if (id != 0) {
                try { views.setOnClickPendingIntent(id, settingsPi) } catch (_: Exception) { }
            }
        }
        try { views.setOnClickPendingIntent(R.id.battery_container, settingsPi) } catch (_: Exception) { }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 0
    }

    private fun isCharging(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    companion object {
        const val PREFS = "small_battery_prefs"
        fun aodKey(id: Int) = "aod_$id"
        const val ACTION_REFRESH = "com.nothing.hearthstone.battery.small.ACTION_REFRESH"
    }
}