package com.patrolshield.data.remote

import android.content.SharedPreferences
import com.patrolshield.data.local.dao.UserDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userDao: UserDao
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Fetch token synchronously (RunBlocking is acceptable for network thread)
        val user = runBlocking { userDao.getUser() }
        
        if (user != null && !user.token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer ${user.token}")
        }

        return chain.proceed(builder.build())
    }
}
