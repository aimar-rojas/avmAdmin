package aimar.rojas.avmadmin.features.register.presentation

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
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun register() {
        val currentState = _uiState.value
        
        // Validaciones
        if (currentState.username.isBlank()) {
            _uiState.value = currentState.copy(error = "El nombre de usuario es requerido")
            return
        }
        
        if (currentState.username.length < 3) {
            _uiState.value = currentState.copy(error = "El nombre de usuario debe tener al menos 3 caracteres")
            return
        }
        
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(error = "El email es requerido")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) {
            _uiState.value = currentState.copy(error = "Email inválido")
            return
        }
        
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(error = "La contraseña es requerida")
            return
        }
        
        if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(error = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        
        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = currentState.copy(error = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            authRepository.register(
                username = currentState.username,
                email = currentState.email,
                password = currentState.password
            )
                .onSuccess { registerResponse ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isSuccess = true,
                        registerResponse = registerResponse
                    )
                }
                .onFailure { exception ->
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al registrar usuario"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class RegisterUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val registerResponse: aimar.rojas.avmadmin.domain.model.RegisterResponse? = null
)
