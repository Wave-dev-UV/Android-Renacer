package com.example.gestrenacer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gestrenacer.models.User
import com.example.gestrenacer.repository.UserRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


class UserViewModelTest {


    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepositorio: UserRepositorio

    @Before
    fun setUp() {
        userRepositorio = mock(UserRepositorio::class.java)
        userViewModel = UserViewModel(userRepositorio)
    }

    val user = User(
        "Juan", "Aristi", "1006054035", "Cédula", "3206355348", "Dir1",
        "SOS", "ContactJuan", "3016355647", "Papá", "DirContact1",
        "Por Llamar", "1", false, "Feligrés"
    )

    val userActualizado = User(
        "Juancho", "Pelao", "10433892", "Cédula", "3206355348", "Dir1",
        "SOS", "ContactJuan", "3016355647", "Papá", "DirContact1",
        "Por Llamar", "1", false, "Gestor"
    )


    @Test
    fun `test guardar usuario`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        userViewModel.crearUsuario(user)
        verify(userRepositorio).saveUser(user)
    }


    @Test
    fun `test get users`() = runBlocking{
        Dispatchers.setMain(UnconfinedTestDispatcher())
        userViewModel.getFeligreses()

        assertEquals(userViewModel.listaUsers.value, null)
    }

    @Test
    fun `test editar usuario`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        userViewModel.editarUsuario(userActualizado)

        verify(userRepositorio).updateUser(userActualizado)
    }




}

