package com.nothing.hearthstone.photo.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nothing.hearthstone.R
import com.nothing.hearthstone.photo.ui.RectanglePhotoSettingActivity

class PhotoWidgetRectangleProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: android.os.Bundle) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = android.content.ComponentName(context, PhotoWidgetRectangleProvider::class.java)
            val ids = mgr.getAppWidgetIds(cn)
            for (id in ids) update(context, mgr, id)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            prefs.remove(photoKey(id))
        }
        prefs.apply()
    }

    private fun update(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val wide = minW >= 110
        val layout = if (wide) R.layout.card_photo_rectangle_2x1 else R.layout.card_photo_rectangle_1x1
        val views = RemoteViews(context.packageName, layout)
        val intent = Intent(context, RectanglePhotoSettingActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pi = android.app.PendingIntent.getActivity(context, appWidgetId, intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.fl, pi)
        // QoL: make the whole photo frame tappable to configure
        try { views.setOnClickPendingIntent(R.id.pt_container, pi) } catch (_: Exception) { }
        try { views.setOnClickPendingIntent(R.id.view_pager, pi) } catch (_: Exception) { }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val PREFS = "photo_rect_prefs"
        fun photoKey(id: Int) = "photo_$id"
    }
}