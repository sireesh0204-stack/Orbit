package com.portfolio.jobpilot.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer
    private var currentToken: String? = null

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `intercept injects Bearer header when token is present`() {
        currentToken = "test_firebase_id_token_xyz"
        val interceptor = AuthInterceptor { currentToken }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder().url(server.url("/api/v1/profile")).build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        val authHeader = recorded.getHeader("Authorization")

        assertEquals("Bearer test_firebase_id_token_xyz", authHeader)
    }

    @Test
    fun `intercept does not inject Authorization header when token is null`() {
        currentToken = null
        val interceptor = AuthInterceptor { currentToken }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = Request.Builder().url(server.url("/health")).build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }
}

