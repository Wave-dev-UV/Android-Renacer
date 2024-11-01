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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepositorio: UserRepositorio
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


    fun checkUserAccess(phoneNumber: String, activity: Activity) {
        _progress.value = true
        viewModelScope.launch {
            val userRole = userRepositorio.getUserByPhone(phoneNumber)
            val preferences = activity.getSharedPreferences("auth",Context.MODE_PRIVATE).edit()

            preferences.putString("rol",userRole)
            preferences.apply()

            if (userRole != null) {
                sendVerificationCode(phoneNumber, activity)
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
            _authResult.value = result
        }
    }
}
