package com.example.gestrenacer.view

import NotificationWorker
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gestrenacer.R
import com.example.gestrenacer.repository.UserRepositorio
import com.example.gestrenacer.view.fragment.NoConnectionFragment
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val connectionViewModel: ConnectionViewModel by viewModels()
    private lateinit var userRepositorio: UserRepositorio
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() } // Instancia de FirebaseAuth

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1
    }

    interface Recargable {
        fun recargarDatos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userRepositorio = UserRepositorio(this)


        userRepositorio.clearUserRole()


        checkNotificationPermission()

        setupObservers()
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

    private fun isRoleValid(): Boolean {
        val role = userRepositorio.getUserRole()?.lowercase()
        return role == "gestor" || role == "administrador"
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

    fun logout() {
        userRepositorio.clearUserRole()
        auth.signOut()
        Log.d("MainActivity", "Rol eliminado en logout y usuario desconectado")

    }
}
