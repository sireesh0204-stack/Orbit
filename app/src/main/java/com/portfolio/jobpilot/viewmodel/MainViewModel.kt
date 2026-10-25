package com.portfolio.jobpilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portfolio.jobpilot.data.local.entity.JobEntity
import com.portfolio.jobpilot.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val activeUserEmail: String = "pilot.user1@example.com",
    val syncStatusText: String = "SYNCED",
    val activeFilterText: String = "All Cities • >6 YoE • 7d",
    val jobs: List<JobEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val repository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeJobs()
    }

    private fun observeJobs() {
        viewModelScope.launch {
            repository.getJobsStream().collect { jobList ->
                _uiState.value = _uiState.value.copy(jobs = jobList)
            }
        }
    }

    fun refreshFeed(timeframe: String = "7d") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.refreshJobs(timeframe = timeframe)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, syncStatusText = "SYNCED")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Sync failed"
                    )
                }
            )
        }
    }

    fun toggleFavorite(jobId: String) {
        viewModelScope.launch {
            val currentJob = _uiState.value.jobs.find { it.jobId == jobId } ?: return@launch
            repository.toggleFavorite(jobId, currentJob.isFavorite)
        }
    }
}

