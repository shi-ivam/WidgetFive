package com.nothing.hearthstone.mediaplayer.business

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R

class MediaPlayerProvider : AppWidgetProvider() {

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
            Intent.ACTION_MEDIA_BUTTON -> {
                val mgr = AppWidgetManager.getInstance(context)
                val cn = android.content.ComponentName(context, MediaPlayerProvider::class.java)
                val ids = mgr.getAppWidgetIds(cn)
                for (id in ids) update(context, mgr, id)
            }
        }
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = android.content.ComponentName(context, MediaPlayerProvider::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            for (id in ids) update(context, mgr, id)
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
        val views = RemoteViews(context.packageName, R.layout.card_media_player_2x2)

        val musicIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_MUSIC)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val musicPi = android.app.PendingIntent.getActivity(
            context, appWidgetId, musicIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.media_player_container, musicPi) } catch (_: Exception) { }

        // QoL: try to wire any exposed control surfaces to the music app launcher
        val possibleIds = listOf(
            "media_player_container", "container", "iv_cover", "iv_album_art",
            "btn_play", "btn_pause", "btn_next", "btn_prev", "btn_play_pause",
            "tv_title", "tv_artist", "progress_bar"
        )
        for (name in possibleIds) {
            val id = context.resources.getIdentifier(name, "id", context.packageName)
            if (id != 0) {
                try { views.setOnClickPendingIntent(id, musicPi) } catch (_: Exception) { }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "media_player_prefs"
        fun aodKey(id: Int) = "aod_$id"
        const val ACTION_REFRESH = "com.nothing.hearthstone.mediaplayer.ACTION_REFRESH"
    }
}