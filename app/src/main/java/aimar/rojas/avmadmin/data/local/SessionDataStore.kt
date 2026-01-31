package aimar.rojas.avmadmin.data.local

import aimar.rojas.avmadmin.core.auth.TokenDataStore
import aimar.rojas.avmadmin.domain.model.User
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenDataStore: TokenDataStore,
    private val gson: Gson
) {
    private val dataStore: DataStore<Preferences> = context.sessionDataStore
    
    private val userKey = stringPreferencesKey("current_user")
    private val isCompletedProfileKey = booleanPreferencesKey("is_completed_profile")
    
    suspend fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        dataStore.edit { preferences ->
            preferences[userKey] = userJson
        }
    }
    
    val userFlow: Flow<User?> = dataStore.data
        .map { preferences ->
            val userJson = preferences[userKey]
            if (userJson != null) {
                try {
                    gson.fromJson(userJson, User::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    
    suspend fun getUser(): User? {
        val userJson = dataStore.data.first()[userKey]
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    suspend fun saveIsCompletedProfile(isCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[isCompletedProfileKey] = isCompleted
        }
    }
    
    val isCompletedProfileFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[isCompletedProfileKey] ?: false
        }
    
    suspend fun isCompletedProfile(): Boolean {
        return dataStore.data.first()[isCompletedProfileKey] ?: false
    }
    
    suspend fun clearSession() {
        tokenDataStore.clearToken()
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    val isLoggedInFlow: Flow<Boolean> = tokenDataStore.isLoggedInFlow
    
    suspend fun isLoggedIn(): Boolean {
        return tokenDataStore.isLoggedIn()
    }
    
    suspend fun getToken(): String? {
        return tokenDataStore.getToken()
    }
}
