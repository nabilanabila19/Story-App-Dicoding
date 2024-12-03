package com.nabila.storyappdicoding.data.pref

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val userPreference: UserPreference) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { userPreference.getSession().first().token }
        val request = chain.request()
        val requestHeaders = request.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        Log.d("AuthInterceptor", "Intercepting request: ${request.url}") // Tambahkan log di sini
        Log.d("AuthInterceptor", "Adding Authorization header: Bearer $token")

        return chain.proceed(requestHeaders)
    }
}