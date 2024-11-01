package com.example.gestrenacer.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gestrenacer.R
import com.example.gestrenacer.view.MainActivity

class PendienteNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val pendingUsersCount = inputData.getInt("pendingUsersCount", 0)

        if (isAdminOrManager()) {
            showNotification("Recordatorio", " la última vez que consultaste la app tenías $pendingUsersCount personas en la lista de pendientes.")
        } else {
            Log.d("SimpleNotificationWorker", "El usuario no tiene permisos para ver la notificación.")
        }
        return Result.success()
    }

    private fun isAdminOrManager(): Boolean {

        val sharedPreferences = applicationContext.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("user_role", "")
        Log.d("SimpleNotificationWorker", "Rol obtenido: $userRole")
        return userRole == "Administrador" || userRole == "Gestor"
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "simple_notification_channel"


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Canal de Notificación Simple",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

            val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            with(NotificationManagerCompat.from(applicationContext)) {
                notify(1, notification)
            }
        } else {
            Log.d("SimpleNotificationWorker", "Permiso de notificación no concedido.")
        }
    }
}
