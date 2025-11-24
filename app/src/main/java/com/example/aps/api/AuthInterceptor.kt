package com.example.aps.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()

        Log.d("AuthInterceptor", "Token from provider: ${token?.take(50)}...") // Log first 50 chars
        Log.d("AuthInterceptor", "Token is null: ${token == null}")
        Log.d("AuthInterceptor", "Token is empty: ${token?.isEmpty()}")

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
            Log.d("AuthInterceptor", "Added Authorization header")
        } else {
            Log.d("AuthInterceptor", "NO TOKEN - Authorization header NOT added")
        }

        val request = requestBuilder.build()
        Log.d("AuthInterceptor", "Request URL: ${request.url}")
        Log.d("AuthInterceptor", "Request headers: ${request.headers}")

        return chain.proceed(request)
    }
}