package com.portfolio.jobpilot.data.auth

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FirebaseAuthManagerTest {

    private lateinit var authManager: FirebaseAuthManager

    @Before
    fun setUp() {
        authManager = FirebaseAuthManager(
            allowedEmails = setOf("pilot.user1@example.com", "pilot.user2@example.com"),
            allowedUids = setOf("firebase_uid_user_1", "firebase_uid_user_2")
        )
    }

    @Test
    fun `isUserWhitelisted returns true for whitelisted user 1 email`() {
        assertTrue(authManager.isUserWhitelisted("pilot.user1@example.com", "any_uid"))
    }

    @Test
    fun `isUserWhitelisted returns true for whitelisted user 2 email regardless of case`() {
        assertTrue(authManager.isUserWhitelisted("PILOT.USER2@EXAMPLE.COM", "any_uid"))
    }

    @Test
    fun `isUserWhitelisted returns true for whitelisted uid even if email is unknown`() {
        assertTrue(authManager.isUserWhitelisted("unknown@other.com", "firebase_uid_user_1"))
    }

    @Test
    fun `isUserWhitelisted returns false for non-whitelisted user`() {
        assertFalse(authManager.isUserWhitelisted("stranger@hacker.com", "untrusted_uid"))
    }

    @Test
    fun `updateSession succeeds for whitelisted user and sets token`() {
        val validUser = AuthUser(
            uid = "firebase_uid_user_1",
            email = "pilot.user1@example.com",
            displayName = "User One",
            idToken = "mock_jwt_token_123"
        )
        val success = authManager.updateSession(validUser)
        assertTrue(success)
        assertEquals("mock_jwt_token_123", authManager.getCurrentToken())
    }

    @Test
    fun `updateSession fails and clears user for non-whitelisted user`() {
        val invalidUser = AuthUser(
            uid = "random_uid",
            email = "stranger@external.com",
            displayName = "Stranger",
            idToken = "bad_token"
        )
        val success = authManager.updateSession(invalidUser)
        assertFalse(success)
        assertNull(authManager.getCurrentToken())
    }
}

