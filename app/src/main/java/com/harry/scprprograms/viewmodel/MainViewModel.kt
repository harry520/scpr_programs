package com.harry.scprprograms.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scprprograms.util.Resource
import com.harry.scprprograms.model.Programs
import com.harry.scprprograms.repository.SCPRProgramsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(private val scprProgramsRepository: SCPRProgramsRepository) : ViewModel() {
    private val _programs = MutableStateFlow<Resource<Response<Programs>>>(Resource.empty())
    val programs = _programs.asStateFlow()

    fun getPrograms() = viewModelScope.launch {
        try {
            _programs.value = Resource.success(scprProgramsRepository.getPrograms())
        } catch (e: Exception) {
            _programs.value = Resource.error(e.message.toString(), null)
            Log.d("Harry", e.message.toString())
        }
    }
}