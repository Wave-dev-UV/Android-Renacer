package com.example.gestrenacer.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentStatsBinding
import com.example.gestrenacer.viewmodel.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private lateinit var binding: FragmentStatsBinding
    private val statsViewModel: StatsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inicializa el binding
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val webView: WebView = binding.webView

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true


        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }
        webView.webChromeClient = WebChromeClient()

        val url = "https://graficas-renacer.vercel.app/"
        webView.loadUrl(url)
    }
}
