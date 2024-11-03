package com.example.gestrenacer.view

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.gestrenacer.R
import com.example.gestrenacer.view.fragment.NoConnectionFragment
import com.example.gestrenacer.viewmodel.ConnectionViewModel
import com.example.gestrenacer.viewmodel.UserViewModel
import com.example.gestrenacer.worker.PendienteNotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    private val REQUEST_CODE_POST_NOTIFICATION = 1

    interface Recargable {
        fun recargarDatos()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupObservers()
        checkNotificationPermission()
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

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATION
                )
            } else {
                scheduleSimpleNotification()
            }
        } else {
            scheduleSimpleNotification()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_POST_NOTIFICATION &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            scheduleSimpleNotification()
        }
    }

    private fun scheduleSimpleNotification() {
        userViewModel.updatePendingUsersCount()
        userViewModel.pendingUsersCount.observe(this) { count ->
            val notificationData = Data.Builder()
                .putInt("pendingUsersCount", count)
                .build()

            val notificationRequest: WorkRequest = OneTimeWorkRequestBuilder<PendienteNotificationWorker>()
                .setInitialDelay(12, TimeUnit.HOURS)
                .setInputData(notificationData)
                .build()

            WorkManager.getInstance(this).enqueue(notificationRequest)
            Log.d("MainActivity", "Notification scheduled with count: $count")
        }
    }
}
