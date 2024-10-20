package com.example.gestrenacer.view

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gestrenacer.R
import com.example.gestrenacer.view.fragment.NoConnectionFragment
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val connectionViewModel: ConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupObservers()
    }

    private fun setupObservers() {
        connectionViewModel.isConnected.observe(this) { isConnected ->
            if (isConnected == false) {
                // Mostrar fragmento de conexión perdida
                supportFragmentManager.beginTransaction()
                    .replace(R.id.noConnectionContainer, NoConnectionFragment(), "NoConnectionFragment")
                    .commit()

                // Asegurarte de que el contenedor esté visible
                findViewById<FrameLayout>(R.id.noConnectionContainer).visibility = View.VISIBLE
            } else {
                // Ocultar el fragmento si ya hay conexión
                findViewById<FrameLayout>(R.id.noConnectionContainer).visibility = View.GONE
            }
        }
    }
}
