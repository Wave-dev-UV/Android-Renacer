package com.example.gestrenacer.viewmodel
import androidx.lifecycle.ViewModel
import com.example.gestrenacer.repository.UserRepositorio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatsViewModel  @Inject constructor(
    private val userRepositorio: UserRepositorio
) : ViewModel() {

}