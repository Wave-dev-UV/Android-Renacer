package com.example.gestrenacer.view

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ActivityMainBinding
import com.example.gestrenacer.view.fragment.NoConnectionFragment
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val connectionViewModel: ConnectionViewModel by viewModels()

    interface Recargable {
        fun recargarDatos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupBottomNav()
        setupObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteSharedPreferences("auth")
    }

    private fun setupObservers() {
        connectionViewModel.isConnected.observe(this) { isConnected ->
            if (isConnected == false) {

                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.noConnectionContainer,
                        NoConnectionFragment(),
                        "NoConnectionFragment"
                    )
                    .commit()
                binding.contBottomNav.isVisible = false
                binding.noConnectionContainer.visibility = View.VISIBLE
            } else {

                binding.noConnectionContainer.visibility = View.GONE
                binding.noConnectionContainer.visibility = View.GONE

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationContainer) as? androidx.navigation.fragment.NavHostFragment
                val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment

                if (currentFragment is Recargable) {
                    mostrarBottomNav()
                    currentFragment.recargarDatos()
                }

            }
        }
    }

    private fun mostrarBottomNav(){
        val pref = getSharedPreferences("auth", Context.MODE_PRIVATE)
            ?.getString("rol", "Visualizador")

        if (pref in listOf("Adminsitrador","Gestor")){
            visibilidadBottomBar(true)
        }

    }

    private fun setupBottomNav(){
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navigationContainer) as NavHostFragment
        navController = navHostFragment.navController

        val navView = binding.bottomNavigation

        navView.setupWithNavController(navController)
    }

    fun visibilidadBottomBar(visibilidad: Boolean){
        binding.contBottomNav.isVisible = visibilidad
    }

    fun modVisItemBottomBar(item: Int, visibilidad: Boolean){
        binding.bottomNavigation.menu.findItem(item).isVisible = visibilidad
    }
}
