package com.nothing.hearthstone.breath.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R
import com.nothing.hearthstone.breath.ui.BreathSettingActivity

class BreathFocusProvider : AppWidgetProvider() {

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
                val cn = android.content.ComponentName(context, BreathFocusProvider::class.java)
                val ids = mgr.getAppWidgetIds(cn)
                for (id in ids) update(context, mgr, id)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            prefs.remove(modeKey(id))
        }
        prefs.apply()
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.card_breath_focus)

        val settingsIntent = Intent(context, BreathSettingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val settingsPi = android.app.PendingIntent.getActivity(
            context, appWidgetId, settingsIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val clickTargets = listOf("fl", "tv_breath_name", "iv_breath")
        for (name in clickTargets) {
            val id = context.resources.getIdentifier(name, "id", context.packageName)
            if (id != 0) {
                try { views.setOnClickPendingIntent(id, settingsPi) } catch (_: Exception) { }
            }
        }
        try { views.setOnClickPendingIntent(R.id.fl, settingsPi) } catch (_: Exception) { }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "breath_focus_prefs"
        fun modeKey(id: Int) = "mode_$id"
        const val ACTION_REFRESH = "com.nothing.hearthstone.breath.focus.ACTION_REFRESH"
    }
}