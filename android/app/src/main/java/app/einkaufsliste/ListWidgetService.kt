package app.einkaufsliste

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class ListWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = Factory(applicationContext)

    private class Factory(private val ctx: Context) : RemoteViewsFactory {
        private var items: List<Store.Item> = emptyList()

        override fun onCreate() {}
        override fun onDataSetChanged() { items = Store.openItems(ctx) }
        override fun onDestroy() {}
        override fun getCount() = items.size
        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount() = 1
        override fun getItemId(position: Int) = items.getOrNull(position)?.id?.hashCode()?.toLong() ?: position.toLong()
        override fun hasStableIds() = true

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(ctx.packageName, R.layout.widget_item)
            val item = items.getOrNull(position) ?: return views
            views.setTextViewText(R.id.text, item.text)
            views.setOnClickFillInIntent(R.id.item, Intent().putExtra(ListWidget.EXTRA_ID, item.id))
            return views
        }
    }
}
