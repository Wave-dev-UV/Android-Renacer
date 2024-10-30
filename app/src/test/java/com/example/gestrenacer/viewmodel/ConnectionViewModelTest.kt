package com.example.gestrenacer.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.*

class ConnectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var connectionViewModel: ConnectionViewModel
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var application: Application

    @Before
    fun setUp() {
        application = mock(Application::class.java)
        connectivityManager = mock(ConnectivityManager::class.java)
        `when`(application.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager)

        connectionViewModel = ConnectionViewModel(application)
    }

    @Test
    fun `test isConnected when network is available`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Simula una red disponible
        val networkCapabilities = mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(true)

        val network = mock(Network::class.java)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)

        // Llama al método checkConnection
        connectionViewModel.checkConnection()

        // Verifica que isConnected sea verdadero
        val observer = mock(Observer::class.java) as Observer<Boolean>
        connectionViewModel.isConnected.observeForever(observer)

        // Asegúrate de que el valor sea verdadero
        assertTrue(connectionViewModel.isConnected.value == true)

        // Verifica que el observer haya recibido el valor verdadero
        verify(observer).onChanged(true)

        // Limpia el observer
        connectionViewModel.isConnected.removeObserver(observer)
    }

    @Test
    fun `test isConnected when network is not available`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Simula una red no disponible
        val networkCapabilities = mock(NetworkCapabilities::class.java)
        `when`(networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).thenReturn(false)

        val network = mock(Network::class.java)
        `when`(connectivityManager.activeNetwork).thenReturn(network)
        `when`(connectivityManager.getNetworkCapabilities(network)).thenReturn(networkCapabilities)

        // Llama al método checkConnection
        connectionViewModel.checkConnection()

        // Verifica que isConnected sea falso
        val observer = mock(Observer::class.java) as Observer<Boolean>
        connectionViewModel.isConnected.observeForever(observer)

        // Asegúrate de que el valor sea falso
        assertFalse(connectionViewModel.isConnected.value == true)

        // Verifica que el observer haya recibido el valor falso
        verify(observer).onChanged(false)

        // Limpia el observer
        connectionViewModel.isConnected.removeObserver(observer)
    }
}
