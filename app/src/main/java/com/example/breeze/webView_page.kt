package com.example.breeze

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class webView_page : AppCompatActivity() {

    lateinit var news_content :String
    lateinit var loading :ProgressBar
    lateinit var webView: WebView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_web_view_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

         news_content = intent.getStringExtra("content").toString()

         webView = findViewById(R.id.webView)
         loading = findViewById<ProgressBar>(R.id.progressBar)



        webViewsetup(webView)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun webViewsetup(a: WebView?) {

        // favicon is the samll icon next to tittle of webpage
        // favicon: android.graphics.Bitmap? represents the webpage small icon as a bitmap
        a?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // Show ProgressBar when the page starts loading
                loading.visibility = View.VISIBLE
                webView.visibility = View.INVISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide ProgressBar and show WebView when the page finishes loading
                loading.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }

        a?.apply {
            settings.javaScriptEnabled=true
            settings.safeBrowsingEnabled=true
            loadUrl(news_content)

        }



    }
}