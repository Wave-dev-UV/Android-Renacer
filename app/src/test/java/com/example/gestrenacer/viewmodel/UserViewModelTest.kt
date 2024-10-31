package com.example.gestrenacer.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
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
import com.google.firebase.Timestamp
import org.mockito.Mockito.`when`
import java.util.Calendar
import java.util.Date


class UserViewModelTest {


    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var userViewModel: UserViewModel
    private lateinit var userRepositorio: UserRepositorio

    fun crearTimestamp(año: Int, mes: Int, dia: Int): Timestamp {
        val calendar = Calendar.getInstance().apply {
            set(año, mes - 1, dia, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(calendar.time)
    }


    @Before
    fun setUp() {
        userRepositorio = mock(UserRepositorio::class.java)
        userViewModel = UserViewModel(userRepositorio)

        userViewModel.listaUsers.observeForever { }
    }

    val user = User("Alberto", "Ariza", "11222211", "Cedula", "22111122", "dir11",
        "eps22", "Arturo", "33222233", "papá", "dir22",
        "Por llamar","idk",false, "Feligrés", "Masculino", "Soltero(a)", crearTimestamp(2003, 4, 7), "", Timestamp(Date()))



    val userActualizado = User("Alberto", "Ariza", "11222211", "Cedula", "22111122", "dir11",
        "eps22", "Arturo", "33222233", "papá", "dir22",
        "Por llamar","idk",false, "Feligrés", "Masculino", "Soltero(a)", crearTimestamp(2004, 5, 5 ), "", Timestamp(Date()))


    @Test
    fun `test guardar usuario`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        userViewModel.crearUsuario(user)
        verify(userRepositorio).saveUser(user)
    }


    @Test
    fun `test get users when no matching users found`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val fechaInicial = crearTimestamp(2000, 1, 1)
        val fechaFinal = crearTimestamp(2025, 12, 31)
        val filtroEstcivil = listOf("Soltero(a)")
        val filtroSexo = listOf("Masculino")
        val filtroLlamado = listOf("Por llamar")

        `when`(userRepositorio.getUsers(filtroSexo, filtroEstcivil, filtroLlamado,
            fechaInicial, fechaFinal, "nombre", "ascendente"))
            .thenReturn(emptyList()) // Devuelve una lista vacía


        userViewModel.getFeligreses(fechaInicial, fechaFinal, filtroEstcivil, filtroSexo, filtroLlamado)


        assertNotNull(userViewModel.listaUsers.value)
        assertTrue(userViewModel.listaUsers.value!!.isEmpty()) // Asegúrate de que la lista esté vacía
    }

    @Test
    fun `test get users when matching users found`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val fechaInicial = crearTimestamp(2000, 1, 1)
        val fechaFinal = crearTimestamp(2025, 12, 31)
        val filtroEstcivil = listOf("Soltero(a)")
        val filtroSexo = listOf("Masculino")
        val filtroLlamado = listOf("Por llamar")

        // Simulando que hay usuarios que cumplen con los filtros
        val matchingUsers = listOf(
            User("Alberto", "Ariza", "11222211", "Cedula", "22111122", "dir11",
                "eps22", "Arturo", "33222233", "papá", "dir22",
                "Por llamar", "idk", false, "Feligrés", "Masculino", "Soltero(a)",
                crearTimestamp(2003, 4, 7), "", crearTimestamp(2024, 5, 5))
        )

        `when`(userRepositorio.getUsers(filtroSexo, filtroEstcivil, filtroLlamado,
            fechaInicial, fechaFinal, "nombre", "ascendente"))
            .thenReturn(matchingUsers)


        userViewModel.getFeligreses(fechaInicial, fechaFinal, filtroEstcivil, filtroSexo, filtroLlamado)

        assertNotNull(userViewModel.listaUsers.value)
        assertFalse(userViewModel.listaUsers.value!!.isEmpty())
        assertEquals(1, userViewModel.listaUsers.value!!.size)
        assertEquals("Alberto", userViewModel.listaUsers.value!![0].nombre)
    }


    @Test
    fun `test editar usuario`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        userViewModel.editarUsuario(userActualizado)

        verify(userRepositorio).updateUser(userActualizado)
    }

    //para probar el borrado en lista se hace necesario volver la lista de usuarios publica o crear un metodo para accederla publcamente, lo cual, según entiendo, no es recomendable

    @Test
    fun `test borrar usuario`() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        `when`(userRepositorio.borrarUsuario(user)).thenReturn(Unit)

        userViewModel.borrarUsuario(user)

        verify(userRepositorio).borrarUsuario(user)

    }


}

