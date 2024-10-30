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
import android.util.Log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val connectionViewModel: ConnectionViewModel by viewModels()

    interface Recargable {
        fun recargarDatos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupObservers()
    }

    private fun setupObservers() {
        connectionViewModel.isConnected.observe(this) { isConnected ->
            if (isConnected == false) {

                supportFragmentManager.beginTransaction()
                    .replace(R.id.noConnectionContainer, NoConnectionFragment(), "NoConnectionFragment")
                    .commit()

                findViewById<FrameLayout>(R.id.noConnectionContainer).visibility = View.VISIBLE
            } else {

                findViewById<FrameLayout>(R.id.noConnectionContainer).visibility = View.GONE

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationContainer) as? androidx.navigation.fragment.NavHostFragment
                val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment

                if (currentFragment is Recargable) {
                    currentFragment.recargarDatos()
                }

            }
        }
    }
}
