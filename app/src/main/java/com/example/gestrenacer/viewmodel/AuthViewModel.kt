package com.example.gestrenacer.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestrenacer.repository.UserRepositorio
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _verificationId = MutableLiveData<String>()
    val verificationId: LiveData<String> = _verificationId

    private val _authResult = MutableLiveData<Boolean>()
    val authResult: LiveData<Boolean> = _authResult

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _accessGranted = MutableLiveData<Boolean>()
    val accessGranted: LiveData<Boolean> = _accessGranted

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = _progress

    private val _rol = MutableLiveData("Feligrés")
    val rol: LiveData<String> = _rol

    private val sharedPref = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)

    fun isUserVerified(): Boolean {
        return sharedPref.getBoolean("user_verified", false)
    }

    fun saveUserVerification() {
        with(sharedPref.edit()) {
            putBoolean("user_verified", true)
            apply()
        }
    }

    fun saveUserRole(role: String) {
        with(sharedPref.edit()) {
            putString("user_role", role)
            apply()
        }
    }

    fun getUserRole(): String {
        return sharedPref.getString("user_role", null) ?: "Visualizador"
    }

    fun isReVerificationNeeded(): Boolean {
        val lastVerification = sharedPref.getLong("last_verification_time", 0)
        val currentTime = System.currentTimeMillis()


        return (currentTime - lastVerification) >  2 * 60 * 1000
    }

    fun saveLastVerificationTime() {
        with(sharedPref.edit()) {
            putLong("last_verification_time", System.currentTimeMillis())
            apply()
        }
    }

    fun checkUserAccess(phoneNumber: String, activity: Activity) {
        _progress.value = true
        viewModelScope.launch {
            val userRole = userRepositorio.getUserByPhone(phoneNumber)

            if (userRole != null) {
                sendVerificationCode(phoneNumber, activity)
                _rol.value = userRole
            } else {
                _accessGranted.value = false
                _error.value = "El usuario no tiene acceso o no está registrado"
                _progress.value = false
            }
        }
    }


    private fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        val fullPhoneNumber = "+57$phoneNumber"

        userRepositorio.sendVerificationCode(fullPhoneNumber, object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _error.value = "Error al enviar el código: ${e.message}"
                _progress.value = false
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                _verificationId.value = verificationId
                _progress.value = false
            }
        }, activity)
    }

    fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val result = userRepositorio.signInWithPhoneAuthCredential(credential)
            if (result) {
                saveUserVerification()
                saveUserRole(rol.value ?: "Visualizador")
                saveLastVerificationTime()
            }
            _authResult.value = result
        }
    }
}
