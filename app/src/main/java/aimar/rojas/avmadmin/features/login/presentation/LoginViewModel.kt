package aimar.rojas.avmadmin.features.login.presentation

import aimar.rojas.avmadmin.data.local.LoginPreferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginPreferencesDataStore: LoginPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val rememberedEmail = loginPreferencesDataStore.getRememberedEmail().orEmpty()
            if (rememberedEmail.isNotBlank()) {
                _uiState.value = _uiState.value.copy(
                    email = rememberedEmail,
                    rememberEmail = true
                )
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onRememberEmailChange(rememberEmail: Boolean) {
        _uiState.value = _uiState.value.copy(rememberEmail = rememberEmail)

        if (!rememberEmail) {
            viewModelScope.launch {
                loginPreferencesDataStore.clearRememberedEmail()
            }
        }
    }

    fun login() {
        val currentState = _uiState.value
        
        // Validación básica
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(error = "El email es requerido")
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "La contraseña es requerida")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = currentState.copy(error = "Email inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            authRepository.login(currentState.email, currentState.password)
                .onSuccess { authResponse ->
                    val latestState = _uiState.value

                    if (latestState.rememberEmail) {
                        loginPreferencesDataStore.saveRememberedEmail(latestState.email.trim())
                    } else {
                        loginPreferencesDataStore.clearRememberedEmail()
                    }

                    _uiState.value = latestState.copy(
                        isLoading = false,
                        isSuccess = true,
                        authResponse = authResponse
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al iniciar sesión"
                    )
                }
        }
    }

}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberEmail: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val authResponse: aimar.rojas.avmadmin.domain.model.AuthResponse? = null
)
