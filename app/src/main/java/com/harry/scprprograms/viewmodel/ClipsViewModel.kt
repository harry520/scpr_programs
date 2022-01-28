package com.harry.scprprograms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scprprograms.util.Resource
import com.harry.scprprograms.model.Clips
import com.harry.scprprograms.repository.SCPRProgramsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class ClipsViewModel(private val scprProgramsRepository: SCPRProgramsRepository): ViewModel() {
    private val _programClips = MutableStateFlow<Resource<Response<Clips>>>(Resource.empty())
    val programClips = _programClips.asStateFlow()

    fun getProgramClips(clipId: String) = viewModelScope.launch {
        try {
            _programClips.value = Resource.success(scprProgramsRepository.getProgramClips(clipId))
        } catch (e: Exception) {
            _programClips.value = Resource.error(e.message.toString(), null)
        }
    }
}