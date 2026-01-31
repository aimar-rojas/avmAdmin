# Sistema de Autenticación - AVM Admin

## Resumen

El sistema de autenticación utiliza **JWT tokens** y **DataStore** para almacenar la sesión del usuario. La arquitectura sigue el patrón **Clean Architecture** con separación en capas: Presentation, Domain y Data.

## Componentes Principales

### 1. TokenDataStore
**Ubicación**: `core/auth/TokenDataStore.kt`

Gestiona el token JWT del usuario usando DataStore.

**Funciones principales**:
- `saveToken(token: String)`: Guarda el token
- `getToken(): String?`: Obtiene el token actual
- `clearToken()`: Elimina el token
- `isLoggedIn(): Boolean`: Verifica si hay token guardado
- `tokenFlow: Flow<String?>`: Flow reactivo del token

### 2. SessionDataStore
**Ubicación**: `data/local/SessionDataStore.kt`

Gestiona la sesión completa del usuario (usuario, estado de perfil).

**Funciones principales**:
- `saveUser(user: User)`: Guarda información del usuario
- `getUser(): User?`: Obtiene el usuario actual
- `saveIsCompletedProfile(isCompleted: Boolean)`: Guarda estado del perfil
- `isCompletedProfile(): Boolean`: Verifica si el perfil está completo
- `clearSession()`: Limpia toda la sesión
- `userFlow: Flow<User?>`: Flow reactivo del usuario

### 3. AuthInterceptor
**Ubicación**: `core/auth/AuthInterceptor.kt`

Intercepta automáticamente las peticiones HTTP y agrega el token JWT en el header `Authorization`.

**Funcionamiento**:
- Cachea el token en memoria para acceso rápido
- Observa cambios en el token mediante Flow
- Agrega `Authorization: Bearer <token>` a todas las peticiones (excepto si ya tiene el header)

### 4. AuthRepository
**Ubicación**: `data/repository/AuthRepositoryImpl.kt`

Implementa la lógica de autenticación y comunicación con el backend.

**Funciones**:
- `login(email, password)`: Inicia sesión y guarda token/usuario
- `register(username, email, password)`: Registra nuevo usuario
- `logout()`: Cierra sesión y limpia datos
- `isLoggedIn()`: Verifica estado de autenticación

## Flujo de Login

```
1. Usuario ingresa email/password en LoginScreen
   ↓
2. LoginViewModel valida datos y llama authRepository.login()
   ↓
3. AuthRepositoryImpl hace petición POST /api/v1/login
   ↓
4. Si es exitoso:
   - TokenDataStore.saveToken() → Guarda token JWT
   - SessionDataStore.saveUser() → Guarda usuario
   - SessionDataStore.saveIsCompletedProfile() → Guarda estado
   ↓
5. LoginViewModel actualiza UI con isSuccess = true
   ↓
6. LoginScreen navega a HomeScreen
```

**Respuesta del backend**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  },
  "isCompletedProfile": true
}
```

## Flujo de Register

```
1. Usuario ingresa datos en RegisterScreen
   ↓
2. RegisterViewModel valida datos y llama authRepository.register()
   ↓
3. AuthRepositoryImpl hace petición POST /api/v1/register
   ↓
4. Si es exitoso, RegisterViewModel actualiza UI
   ↓
5. RegisterScreen navega a LoginScreen
   (El usuario debe hacer login después del registro)
```

**Respuesta del backend**:
```json
{
  "message": "Usuario registrado exitosamente",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com"
  }
}
```

## Almacenamiento con DataStore

### Token
- **Archivo**: `auth_prefs`
- **Clave**: `auth_token`
- **Tipo**: String (JWT token)

### Sesión
- **Archivo**: `session_prefs`
- **Claves**:
  - `current_user`: String (JSON serializado del objeto User)
  - `is_completed_profile`: Boolean

### Ventajas de DataStore
- ✅ API asíncrona (no bloquea el hilo principal)
- ✅ Type safety
- ✅ Soporte para Flows (reactivo)
- ✅ Manejo robusto de errores

## Interceptor Automático

El `AuthInterceptor` se configura en `NetWorkModule` y se aplica a todas las peticiones HTTP automáticamente:

```kotlin
OkHttpClient.Builder()
    .addInterceptor(authInterceptor)  // ← Agrega token automáticamente
    .build()
```

**Comportamiento**:
- Si la petición ya tiene header `Authorization`, no lo modifica
- Si hay token guardado, agrega `Authorization: Bearer <token>`
- Si no hay token, la petición se envía sin el header

## Estructura de Archivos

```
core/auth/
  ├── TokenDataStore.kt      # Gestión de tokens
  └── AuthInterceptor.kt     # Interceptor HTTP

data/local/
  └── SessionDataStore.kt    # Gestión de sesión

data/repository/
  └── AuthRepositoryImpl.kt  # Lógica de autenticación

features/login/
  └── presentation/
      ├── LoginScreen.kt
      └── LoginViewModel.kt

features/register/
  └── presentation/
      ├── RegisterScreen.kt
      └── RegisterViewModel.kt
```

## Notas Importantes

- El token JWT tiene validez de **7 días**
- Después del registro, el usuario **debe hacer login** para obtener un token
- El `AuthInterceptor` cachea el token en memoria para acceso rápido
- Todos los métodos de DataStore son `suspend` (coroutines)
- Los Flows permiten UI reactiva que se actualiza automáticamente
