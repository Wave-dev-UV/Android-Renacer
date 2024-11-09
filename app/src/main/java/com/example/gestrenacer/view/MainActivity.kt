package com.example.gestrenacer.view

import NotificationWorker
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestrenacer.R
import com.example.gestrenacer.databinding.ActivityMainBinding
import com.example.gestrenacer.repository.UserRepositorio
import com.example.gestrenacer.view.fragment.NoConnectionFragment
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() } // Instancia de FirebaseAuth

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    interface Recargable {
        fun recargarDatos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupBottomNav()

        checkNotificationPermission()

        setupObservers()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteSharedPreferences("filtros")
        deleteSharedPreferences("filtrosPending")
        deleteSharedPreferences("auth")
    }

    override fun onStop() {
        super.onStop()
        if (auth.currentUser != null && isRoleValid()) {
            scheduleNotification()
        } else {
            Log.d("MainActivity", "No se programará notificación: usuario no autenticado o rol no válido.")
        }
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

                val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationContainer) as? androidx.navigation.fragment.NavHostFragment
                val currentFragment = navHostFragment?.childFragmentManager?.primaryNavigationFragment

                if (currentFragment is Recargable) {
                    mostrarBottomNav()
                    currentFragment.recargarDatos()
                }
            }
        }
    }

    private fun isRoleValid(): Boolean {
        val preferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val role = preferences.getString("rol","Visualizador")

        return role == "Gestor" || role == "Administrador"
    }

    private fun scheduleNotification() {
        val notificationWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(8, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(notificationWork)
        Log.d("MainActivity", "Notificación programada.")
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("MainActivity", "Permiso de notificaciones concedido")
            } else {
                Log.d("MainActivity", "Permiso de notificaciones denegado")
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
