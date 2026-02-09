package com.patrolshield.data.remote

import com.patrolshield.common.SecurePreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val securePrefs: SecurePreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        val token = securePrefs.getToken()
        
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
