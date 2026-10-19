package com.nothing.hearthstone.community.ui

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class CommunityWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, CommunityWidgetProvider::class.java)
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
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val layout = if (minW >= 250 || minH >= 250) R.layout.widget_community_default_2x4 else R.layout.widget_community_default
        val views = RemoteViews(context.packageName, layout)
        val isWide = minW >= 250 || minH >= 250
        // QoL: make the whole community surface tappable to open the community app
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pi = if (intent != null) {
            android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        } else {
            null
        }
        if (pi != null) {
            try { views.setOnClickPendingIntent(R.id.default_root_rl, pi) } catch (_: Exception) { }
            try { views.setOnClickPendingIntent(R.id.iv, pi) } catch (_: Exception) { }
            try { views.setOnClickPendingIntent(R.id.title, pi) } catch (_: Exception) { }
            if (isWide) {
                try { views.setOnClickPendingIntent(R.id.author, pi) } catch (_: Exception) { }
            }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "community_prefs"
        fun aodKey(id: Int) = "aod_$id"
    }
}