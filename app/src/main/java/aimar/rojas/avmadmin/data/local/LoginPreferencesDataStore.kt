package aimar.rojas.avmadmin.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.loginPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "login_preferences")

@Singleton
class LoginPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.loginPreferencesDataStore

    private val rememberedEmailKey = stringPreferencesKey("remembered_email")

    suspend fun getRememberedEmail(): String? {
        return dataStore.data.first()[rememberedEmailKey]
    }

    suspend fun saveRememberedEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[rememberedEmailKey] = email
        }
    }

    suspend fun clearRememberedEmail() {
        dataStore.edit { preferences ->
            preferences.remove(rememberedEmailKey)
        }
    }
}
