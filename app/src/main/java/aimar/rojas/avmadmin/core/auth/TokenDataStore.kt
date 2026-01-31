package aimar.rojas.avmadmin.core.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.authDataStore
    
    private val tokenKey = stringPreferencesKey("auth_token")
    
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }
    
    val tokenFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[tokenKey]
        }
    
    suspend fun getToken(): String? {
        return dataStore.data.first()[tokenKey]
    }
    
    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
    
    val isLoggedInFlow: Flow<Boolean> = tokenFlow.map { it != null }
    
    suspend fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
