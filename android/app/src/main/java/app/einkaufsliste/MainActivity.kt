package app.einkaufsliste

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import org.json.JSONObject

class MainActivity : Activity() {

    private lateinit var web: WebView
    private var pageReady = false
    private var listenWhenReady = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.addJavascriptInterface(Bridge(), "Android")
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                pageReady = true
                if (listenWhenReady) { listenWhenReady = false; listen() }
            }
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    override fun onResume() {
        super.onResume()
        // Im Widget abgehakte Einträge übernehmen
        if (pageReady) web.evaluateJavascript("window.reloadFromNative && reloadFromNative()", null)
    }

    private fun handle(intent: Intent?) {
        if (intent == null || intent.action != ACTION_SPEAK) return
        intent.action = null
        if (pageReady) listen() else listenWhenReady = true
    }

    private fun listen() {
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Was brauchst du?")
        try {
            @Suppress("DEPRECATION")
            startActivityForResult(i, REQ_SPEECH)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Keine Spracherkennung gefunden. Bitte die Google-App installieren.", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Activity-Result-API braucht AndroidX; hier reicht die klassische Variante")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_SPEECH || resultCode != RESULT_OK) return
        val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: return
        web.evaluateJavascript("window.onSpoken(" + JSONObject.quote(text) + ")", null)
    }

    inner class Bridge {
        @JavascriptInterface fun load(): String = Store.itemsJson(this@MainActivity)
        @JavascriptInterface fun save(json: String) = Store.saveItems(this@MainActivity, json)
        @JavascriptInterface fun loadOrder(): String = Store.orderJson(this@MainActivity)
        @JavascriptInterface fun saveOrder(json: String) = Store.saveOrder(this@MainActivity, json)
        @JavascriptInterface fun listen() = runOnUiThread { this@MainActivity.listen() }
    }

    companion object {
        const val ACTION_SPEAK = "app.einkaufsliste.SPRECHEN"
        private const val REQ_SPEECH = 7
    }
}
