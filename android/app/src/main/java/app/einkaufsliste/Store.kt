package app.einkaufsliste

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import org.json.JSONArray

/** Gemeinsamer Speicher von App und Widget. Die Web-Oberfläche liest und schreibt dasselbe JSON. */
object Store {
    private const val PREFS = "einkaufsliste"
    private const val ITEMS = "items"
    private const val ORDER = "reihenfolge"

    data class Item(val id: String, val text: String, val cat: String, val ts: Long)

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun itemsJson(ctx: Context): String = prefs(ctx).getString(ITEMS, "[]") ?: "[]"
    fun orderJson(ctx: Context): String = prefs(ctx).getString(ORDER, "null") ?: "null"

    fun saveItems(ctx: Context, json: String) {
        prefs(ctx).edit().putString(ITEMS, json).apply()
        ListWidget.refresh(ctx)
    }

    fun saveOrder(ctx: Context, json: String) {
        prefs(ctx).edit().putString(ORDER, json).apply()
        ListWidget.refresh(ctx)
    }

    /** Offene Einträge, sortiert nach der Reihenfolge im Laden. */
    fun openItems(ctx: Context): List<Item> {
        val order = try {
            val a = JSONArray(orderJson(ctx)); List(a.length()) { a.getString(it) }
        } catch (e: Exception) { emptyList() }
        val arr = try { JSONArray(itemsJson(ctx)) } catch (e: Exception) { JSONArray() }
        val list = mutableListOf<Item>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optBoolean("done")) continue
            list += Item(o.optString("id"), o.optString("text"), o.optString("cat", "Sonstiges"), o.optLong("ts"))
        }
        fun rank(cat: String) = order.indexOf(cat).let { if (it < 0) Int.MAX_VALUE else it }
        return list.sortedWith(compareBy({ rank(it.cat) }, { it.ts }))
    }

    /** Hakt einen Eintrag ab (aus dem Widget heraus). */
    fun markDone(ctx: Context, id: String) {
        val arr = try { JSONArray(itemsJson(ctx)) } catch (e: Exception) { return }
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.optString("id") == id) o.put("done", true)
        }
        saveItems(ctx, arr.toString())
    }

    fun widgetIds(ctx: Context): IntArray =
        AppWidgetManager.getInstance(ctx).getAppWidgetIds(ComponentName(ctx, ListWidget::class.java))
}
