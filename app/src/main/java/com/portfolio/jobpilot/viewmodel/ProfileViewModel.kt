package com.portfolio.jobpilot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.portfolio.jobpilot.data.remote.dto.SkillsGroupDto
import com.portfolio.jobpilot.data.remote.dto.UserProfileDto
import com.portfolio.jobpilot.data.remote.dto.UserProfilePatchDto
import com.portfolio.jobpilot.data.repository.JobRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfileDto? = null,
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.fetchProfile()
            result.fold(
                onSuccess = { profile ->
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Failed to load profile"
                    )
                }
            )
        }
    }

    fun uploadResume(fileName: String, fileBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorMessage = null,
                statusMessage = "Extracting & structuring resume..."
            )
            val result = repository.uploadResume(fileName, fileBytes)
            result.fold(
                onSuccess = { parsedProfile ->
                    _uiState.value = _uiState.value.copy(
                        profile = parsedProfile,
                        isUploading = false,
                        statusMessage = "Resume successfully structured!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        statusMessage = null,
                        errorMessage = error.localizedMessage ?: "Failed to upload and parse resume"
                    )
                }
            )
        }
    }

    fun uploadGoogleDoc(url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                errorMessage = null,
                statusMessage = "Importing document from Google Docs / Drive..."
            )
            val result = repository.uploadGoogleDoc(url)
            result.fold(
                onSuccess = { parsedProfile ->
                    _uiState.value = _uiState.value.copy(
                        profile = parsedProfile,
                        isUploading = false,
                        statusMessage = "Google Doc successfully imported & structured!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        statusMessage = null,
                        errorMessage = error.localizedMessage ?: "Google Docs import failed"
                    )
                }
            )
        }
    }

    fun updateTargetRoles(roles: List<String>) {
        val current = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(profile = current.copy(targetRoles = roles))
    }

    fun updateYearsExperience(years: Double) {
        val current = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(profile = current.copy(totalYearsExperience = years))
    }

    fun updateRemotePreference(isRemote: Boolean) {
        val current = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(profile = current.copy(isRemotePreferred = isRemote))
    }

    fun updateCities(cities: List<String>) {
        val current = _uiState.value.profile ?: return
        _uiState.value = _uiState.value.copy(profile = current.copy(preferredCities = cities))
    }

    fun saveProfile() {
        val current = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val patch = UserProfilePatchDto(
                targetRoles = current.targetRoles,
                totalYearsExperience = current.totalYearsExperience,
                preferredCities = current.preferredCities,
                isRemotePreferred = current.isRemotePreferred,
                skills = current.skills,
                domains = current.domains,
                workHistory = current.workHistory,
                education = current.education
            )
            val result = repository.patchProfile(patch)
            result.fold(
                onSuccess = { savedProfile ->
                    _uiState.value = _uiState.value.copy(
                        profile = savedProfile,
                        isSaving = false,
                        statusMessage = "Profile changes saved."
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.localizedMessage ?: "Save failed"
                    )
                }
            )
        }
    }
}
