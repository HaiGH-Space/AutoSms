package com.gmail.nthai.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.nthai.domain.model.HomeDashboardStats
import com.gmail.nthai.domain.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeDashboardViewModel @Inject constructor(
    customerRepository: CustomerRepository
) : ViewModel() {

    val stats: StateFlow<HomeDashboardStats> = customerRepository.observeDashboardStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeDashboardStats())
}
