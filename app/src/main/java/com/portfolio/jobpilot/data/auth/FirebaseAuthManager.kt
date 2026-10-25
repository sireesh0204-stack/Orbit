package com.portfolio.jobpilot.data.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String = "",
    val idToken: String = ""
)

class FirebaseAuthManager(
    private val allowedEmails: Set<String> = setOf(
        "sireesh0204@gmail.com",
        "bhargaviyerroju@outlook.com"
    ),
    private val allowedUids: Set<String> = setOf(
        "firebase_uid_user_1",
        "firebase_uid_user_2"
    )
) {
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    fun isUserWhitelisted(email: String?, uid: String?): Boolean {
        val emailAllowed = email?.trim()?.lowercase()?.let { it in allowedEmails } ?: false
        val uidAllowed = uid?.trim()?.let { it in allowedUids } ?: false
        return emailAllowed || uidAllowed
    }

    fun updateSession(user: AuthUser?): Boolean {
        if (user == null) {
            _currentUser.value = null
            return true
        }
        
        if (isUserWhitelisted(user.email, user.uid)) {
            _currentUser.value = user
            return true
        } else {
            _currentUser.value = null
            return false
        }
    }

    fun getCurrentToken(): String? {
        return _currentUser.value?.idToken
    }
}

