package aimar.rojas.avmadmin.data.local

import aimar.rojas.avmadmin.core.auth.TokenManager
import aimar.rojas.avmadmin.domain.model.User
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun saveIsCompletedProfile(isCompleted: Boolean) {
        prefs.edit().putBoolean(KEY_IS_COMPLETED_PROFILE, isCompleted).apply()
    }

    fun isCompletedProfile(): Boolean {
        return prefs.getBoolean(KEY_IS_COMPLETED_PROFILE, false)
    }

    fun clearSession() {
        tokenManager.clearToken()
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    fun getToken(): String? {
        return tokenManager.getToken()
    }

    companion object {
        private const val PREFS_NAME = "session_prefs"
        private const val KEY_USER = "current_user"
        private const val KEY_IS_COMPLETED_PROFILE = "is_completed_profile"
    }
}
