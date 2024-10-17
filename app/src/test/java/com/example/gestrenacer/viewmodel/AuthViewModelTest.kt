package com.example.gestrenacer.viewmodel

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestrenacer.repository.UserRepositorio
import com.google.firebase.auth.PhoneAuthCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock



class AuthViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userRepositorio: UserRepositorio
    private val activity: Activity = mock(Activity::class.java)

    @Before
    fun setUp() {
        userRepositorio = mock(UserRepositorio::class.java)
        authViewModel = AuthViewModel(userRepositorio)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `test acceso exitoso`() = runBlocking {
        val phoneNumber = "3206355348"
        val userRole = "Feligrés"

        doReturn(userRole).`when`(userRepositorio).getUserByPhone(phoneNumber)

        authViewModel.checkUserAccess(phoneNumber, activity)

        assertEquals(userRole, authViewModel.rol.value)
        assertTrue(authViewModel.progress.value == true)
        assertNull(authViewModel.error.value)
        assertTrue(authViewModel.accessGranted.value == null)
    }

    @Test
    fun `test acceso fallido`() = runBlocking {
        val phoneNumber = "3206355348"

        doReturn(null).`when`(userRepositorio).getUserByPhone(phoneNumber)
        authViewModel.checkUserAccess(phoneNumber, activity)

        assertFalse(authViewModel.accessGranted.value == true)
        assertEquals("El usuario no tiene acceso o no está registrado", authViewModel.error.value)
        assertFalse(authViewModel.progress.value == true)
    }

    @Test
    fun `test sign in con credencial`() = runBlocking {
        val credential = mock(PhoneAuthCredential::class.java)

        doReturn(true).`when`(userRepositorio).signInWithPhoneAuthCredential(credential)
        authViewModel.signInWithCredential(credential)

        assertTrue(authViewModel.authResult.value == true)
    }

}
