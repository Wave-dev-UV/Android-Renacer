package com.example.gestrenacer.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    init {
        checkConnection()
    }

    fun checkConnection() {
        viewModelScope.launch {
            val connectivityManager =
                application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkCapabilities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.activeNetwork?.let { network ->
                    connectivityManager.getNetworkCapabilities(network)
                }
            } else {
                null
            }

            _isConnected.value = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
    }
}
