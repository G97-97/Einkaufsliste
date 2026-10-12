package app.einkaufsliste

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class ListWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(ctx, manager, it) }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        if (intent.action == ACTION_DONE) {
            intent.getStringExtra(EXTRA_ID)?.let { Store.markDone(ctx, it) }
            return
        }
        super.onReceive(ctx, intent)
    }

    companion object {
        const val ACTION_DONE = "app.einkaufsliste.ERLEDIGT"
        const val EXTRA_ID = "id"

        fun refresh(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = Store.widgetIds(ctx)
            if (ids.isEmpty()) return
            ids.forEach { update(ctx, manager, it) }
            @Suppress("DEPRECATION")
            manager.notifyAppWidgetViewDataChanged(ids, R.id.list)
        }

        private fun update(ctx: Context, manager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(ctx.packageName, R.layout.widget)
            val open = Store.openItems(ctx).size
            views.setTextViewText(R.id.title, if (open == 0) "Einkaufsliste" else "Einkaufsliste · $open offen")

            // Mikrofon: App öffnen und sofort zuhören
            val speak = Intent(ctx, MainActivity::class.java)
                .setAction(MainActivity.ACTION_SPEAK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            views.setOnClickPendingIntent(R.id.mic,
                PendingIntent.getActivity(ctx, 1, speak, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            // Titel: App öffnen
            val openApp = Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            views.setOnClickPendingIntent(R.id.title,
                PendingIntent.getActivity(ctx, 2, openApp, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

            // Liste mit Abhaken per Tipp
            val service = Intent(ctx, ListWidgetService::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            service.data = Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME))
            @Suppress("DEPRECATION")
            views.setRemoteAdapter(R.id.list, service)
            views.setEmptyView(R.id.list, R.id.empty)
            val done = Intent(ctx, ListWidget::class.java).setAction(ACTION_DONE)
            views.setPendingIntentTemplate(R.id.list,
                PendingIntent.getBroadcast(ctx, 3, done, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))

            manager.updateAppWidget(widgetId, views)
        }
    }
}
