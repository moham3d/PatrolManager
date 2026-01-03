import com.patrolshield.common.SecurePreferences
import com.patrolshield.data.local.dao.UserDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val userDao: UserDao,
    private val securePrefs: SecurePreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Fetch token securely
        val token = securePrefs.getToken()
        
        if (!token.isNullOrEmpty()) {
            builder.header("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }
}
