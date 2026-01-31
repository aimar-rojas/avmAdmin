# Arquitectura de Autenticación - Recomendación

## Análisis de la Situación Actual

### Problema Identificado
- Hay duplicación: `UserRepository` está en `features/login/domain/` y `features/register/domain/`
- `SessionRepository` está en `domain/repository/` (general) pero está vacío
- La autenticación es transversal (se usa en toda la app) pero también tiene features específicas (login, register)

### ¿Qué debe ser General vs Feature?

**GENERAL (Core/Infraestructura):**
- ✅ Token management (guardar, obtener, eliminar)
- ✅ AuthInterceptor (agregar token a todas las peticiones)
- ✅ AuthRepository (lógica de autenticación)
- ✅ AuthStateManager (estado de sesión)
- ✅ SessionStore (almacenamiento local)

**FEATURE (UI específica):**
- ✅ Login Screen (presentation)
- ✅ Register Screen (presentation)
- ✅ ViewModels específicos de login/register

## Estructura Recomendada

```
app/src/main/java/aimar/rojas/avmadmin/
├── core/
│   ├── auth/                    # 🆕 Infraestructura de auth
│   │   ├── AuthInterceptor.kt   # Interceptor para agregar token
│   │   └── TokenManager.kt      # Gestión de token
│   ├── di/
│   │   ├── NetWorkModule.kt
│   │   └── AuthModule.kt        # 🆕 Módulo DI para auth
│   └── navigation/
│
├── data/
│   ├── local/
│   │   └── SessionStore.kt      # Actualizar para token
│   ├── remote/
│   │   └── api/
│   │       └── AuthApiService.kt  # 🆕 API service
│   └── repository/
│       └── AuthRepositoryImpl.kt  # 🆕 Implementación
│
├── domain/
│   ├── model/
│   │   ├── AuthResponse.kt      # 🆕 Respuesta de login
│   │   ├── LoginRequest.kt       # 🆕 Request de login
│   │   └── RegisterRequest.kt   # 🆕 Request de register
│   └── repository/
│       └── AuthRepository.kt    # 🆕 Interfaz general
│
└── features/
    ├── login/
    │   └── presentation/        # Solo UI
    │       ├── LoginScreen.kt
    │       └── LoginViewModel.kt
    └── register/
        └── presentation/       # Solo UI
            ├── RegisterScreen.kt
            └── RegisterViewModel.kt
```

## Flujo de Autenticación

### 1. Login/Register (Feature)
```
LoginScreen → LoginViewModel → AuthRepository → AuthApiService → Backend
                                      ↓
                              TokenManager (guarda token)
```

### 2. Peticiones Protegidas (Automático)
```
Cualquier API Call → AuthInterceptor → Agrega token automáticamente
```

### 3. Verificación de Sesión
```
App Start → TokenManager.getToken() → Si existe → Usuario autenticado
```

## Ventajas de esta Estructura

1. **Separación de Responsabilidades**
   - Infraestructura (core) separada de UI (features)
   - Fácil de testear
   - Reutilizable

2. **Escalabilidad**
   - Fácil agregar nuevos endpoints de auth
   - Fácil cambiar el almacenamiento del token
   - Fácil agregar refresh token en el futuro

3. **Mantenibilidad**
   - Todo lo relacionado con auth en un lugar
   - Features solo se preocupan de la UI
   - Cambios en auth no afectan features

## Implementación Propuesta

### 1. Modelos de Dominio
- `AuthResponse`: token, user, isCompletedProfile
- `LoginRequest`: email, password
- `RegisterRequest`: username, email, password

### 2. AuthApiService
```kotlin
interface AuthApiService {
    @POST("v1/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("v1/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
```

### 3. AuthInterceptor
```kotlin
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    // Agrega "Authorization: Bearer <token>" a todas las peticiones
}
```

### 4. TokenManager
```kotlin
interface TokenManager {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun isLoggedIn(): Boolean
}
```

### 5. AuthRepository
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(username: String, email: String, password: String): Result<RegisterResponse>
    fun logout()
    fun isLoggedIn(): Boolean
    fun getCurrentToken(): String?
}
```

## Próximos Pasos

1. ✅ Crear modelos de dominio
2. ✅ Crear AuthApiService
3. ✅ Implementar TokenManager
4. ✅ Crear AuthInterceptor
5. ✅ Implementar AuthRepository
6. ✅ Configurar DI
7. ✅ Actualizar SessionStore
8. ✅ Crear ViewModels de login/register
