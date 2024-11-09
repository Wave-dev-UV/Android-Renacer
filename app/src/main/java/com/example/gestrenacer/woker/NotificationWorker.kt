import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.gestrenacer.R
import com.example.gestrenacer.view.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("user_role", null)?.lowercase()

        if (role == "gestor" || role == "administrador") {

            val pendingUsersCount = runBlocking {
                getPendingUsersCount()
            }

            // Verificar si el conteo de personas pendientes es mayor a 0
            if (pendingUsersCount > 0) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        applicationContext,
                        0,
                        intent,
                        0
                    )
                }

                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        "notification_channel",
                        "App Notification",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val notification = NotificationCompat.Builder(applicationContext, "notification_channel")
                    .setContentTitle("Recordatorio")
                    .setContentText("Tienes personas pendientes por llamar")
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentIntent(pendingIntent)
                    .build()

                notificationManager.notify(1, notification)
            } else {
                Log.d("NotificationWorker", "No hay personas pendientes por llamar.")
            }
            return Result.success()
        } else {
            Log.d("NotificationWorker", "Rol no válido para notificación: $role")
            return Result.failure()
        }
    }

    private suspend fun getPendingUsersCount(): Int {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users")
                .whereEqualTo("estadoAtencion", "Por Llamar")
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("NotificationWorker", "Error al obtener el conteo de personas pendientes: ${e.message}")
            0
        }
    }
}
