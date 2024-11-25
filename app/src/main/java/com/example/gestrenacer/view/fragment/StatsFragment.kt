package com.example.gestrenacer.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.FragmentStatsBinding
import com.example.gestrenacer.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats), MainActivity.Recargable {

    private lateinit var binding: FragmentStatsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inicializa el binding
        binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun recargarDatos() {
        anadirRol()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        manejadorBtnVolver()
        conectarWeb()
        anadirRol()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    private fun anadirRol() {
        val pref = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador") as String
        val roles = pref in listOf("Administrador", "Gestor")
        val actividad = activity as MainActivity

        if (roles) {
            actividad.visibilidadBottomBar(true)
        }

        if (pref == "Gestor" || pref == "Visualizador") {
            actividad.modVisItemBottomBar(R.id.statsFragment, false)
        }
    }

    private fun conectarWeb() {
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

    private fun manejadorBtnVolver() {
        binding.toolbar.lblToolbar.text = getString(R.string.tituloReportes)
        binding.toolbar.btnVolver.isVisible = false
    }
}
