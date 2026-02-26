package aimar.rojas.avmadmin

import aimar.rojas.avmadmin.domain.repository.AuthRepository
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    var startDestination by mutableStateOf("login")
        private set

    init {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            startDestination = if (isLoggedIn) "home" else "login"

            _isLoading.value = false
        }
    }
}