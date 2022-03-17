package com.harry.scprprograms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.harry.scprprograms.repository.SCPRProgramsRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelProviderFactory(private val scprProgramsRepository: SCPRProgramsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        MainViewModel(scprProgramsRepository) as T
}