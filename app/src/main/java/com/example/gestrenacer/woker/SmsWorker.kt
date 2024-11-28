import android.content.Context
import android.telephony.SmsManager
import androidx.work.Worker
import androidx.work.WorkerParameters

class SmsWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val usuarios = inputData.getStringArray("usuarios")?.toList() as List<String>
        val mensaje = inputData.getString("mensaje") as String

        try {
            val res = enviarSms(
                    usuarios, mensaje
                )

            if (!res) {
                return Result.failure()
            }

            return Result.success()
        } catch (e: Exception) {
            println("Excepcion SMS: $e")
            return Result.failure()
        }
    }

    fun enviarSms(usuarios: List<String>, mensaje: String): Boolean {
        try {
            val smsManager = SmsManager.getDefault()
            for (phoneNumber in usuarios) {
                smsManager.sendTextMessage("+57${phoneNumber}", null, mensaje, null, null)
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
