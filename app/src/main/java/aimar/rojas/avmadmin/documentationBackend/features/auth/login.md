# Login - Inicio de Sesión

## Descripción
Endpoint para autenticar usuarios y obtener un token JWT de acceso.

## Endpoint
```
POST /api/v1/login
```

## Autenticación
No requiere autenticación (endpoint público)

## Request Body
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Campos Requeridos
- `email` (string): Correo electrónico del usuario.
- `password` (string): Contraseña del usuario.

## Response

### Success (200 OK)
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

#### Campos de Respuesta
- `token` (string): Token JWT para autenticación en endpoints protegidos. Válido por 7 días.
- `user` (object): Información básica del usuario autenticado.
- `isCompletedProfile` (boolean): Indica si el usuario tiene datos de Boss completados.
  - `true`: El usuario tiene datos de Boss registrados. Puede hacer petición a `/profile` para obtenerlos.
  - `false`: El usuario no tiene datos de Boss. Debe completar su perfil.

### Error (401 Unauthorized)
```json
{
  "error": "credenciales inválidas"
}
```

## Ejemplo de Uso

### cURL
```bash
curl -X POST http://localhost:5001/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### JavaScript (Fetch)
```javascript
const response = await fetch('http://localhost:5001/api/v1/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'john@example.com',
    password: 'password123'
  })
});

const data = await response.json();
// Guardar el token para usar en peticiones protegidas
localStorage.setItem('token', data.token);
```

## Uso del Token
El token debe incluirse en el header `Authorization` de las peticiones protegidas:
```
Authorization: Bearer <token>
```

## Notas
- El token JWT tiene una validez de 7 días
- Si `isCompletedProfile` es `true`, el usuario puede obtener sus datos de Boss con `GET /api/v1/profile`
- Si `isCompletedProfile` es `false`, el usuario debe completar su perfil antes de usar funcionalidades que requieran datos de Boss
