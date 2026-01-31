# SessionStore - Explicación

## ¿Para qué sirve SessionStore?

`SessionStore` es una clase que gestiona el **almacenamiento local de la sesión del usuario** en la aplicación. Actúa como una capa de abstracción sobre SharedPreferences para guardar información relacionada con la sesión del usuario autenticado.

### Responsabilidades:

1. **Almacenar información del usuario actual**
   - Guarda el objeto `User` completo después del login
   - Permite acceder al usuario sin hacer peticiones al backend

2. **Guardar estado de perfil completado**
   - Almacena si el usuario tiene datos de Boss completados (`isCompletedProfile`)
   - Útil para determinar qué funcionalidades puede usar el usuario

3. **Gestionar el ciclo de vida de la sesión**
   - `clearSession()`: Limpia toda la información de sesión (token, usuario, estado)
   - `isLoggedIn()`: Verifica si hay una sesión activa
   - `getToken()`: Obtiene el token actual (delegado a TokenManager)

### ¿Por qué separar TokenManager y SessionStore?

- **TokenManager**: Solo gestiona el token JWT (más simple, solo string)
- **SessionStore**: Gestiona información más compleja de la sesión (usuario, estado, etc.)

### Flujo de uso:

```kotlin
// Después del login exitoso
sessionStore.saveUser(authResponse.user)
sessionStore.saveIsCompletedProfile(authResponse.isCompletedProfile)

// En cualquier parte de la app
val currentUser = sessionStore.getUser()
val hasProfile = sessionStore.isCompletedProfile()

// Al hacer logout
sessionStore.clearSession() // Limpia todo
```

### Ventajas:

✅ **Persistencia**: La información sobrevive a reinicios de la app
✅ **Rendimiento**: No necesita hacer peticiones al backend para obtener usuario básico
✅ **Centralizado**: Un solo lugar para gestionar toda la información de sesión
✅ **Abstracción**: Oculta la implementación de SharedPreferences
