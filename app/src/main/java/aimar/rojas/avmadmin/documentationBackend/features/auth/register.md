# Register - Registro de Usuario

## Descripción
Endpoint para registrar nuevos usuarios en el sistema.

## Endpoint
```
POST /api/v1/register
```

## Autenticación
No requiere autenticación (endpoint público)

## Request Body
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Campos Requeridos
- `username` (string): Nombre de usuario. Mínimo 3 caracteres, máximo 50 caracteres.
- `email` (string): Correo electrónico válido. Debe ser único en el sistema.
- `password` (string): Contraseña. Mínimo 6 caracteres.

## Response

### Success (201 Created)
```json
{
  "message": "Usuario registrado exitosamente",
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

### Error (400 Bad Request)
```json
{
  "error": "el email ya está registrado"
}
```

```json
{
  "error": "el username ya está registrado"
}
```

```json
{
  "error": "Key: 'RegisterRequest.Username' Error:Field validation for 'Username' failed on the 'min' tag"
}
```

## Ejemplo de Uso

### cURL
```bash
curl -X POST http://localhost:5001/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### JavaScript (Fetch)
```javascript
const response = await fetch('http://localhost:5001/api/v1/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'johndoe',
    email: 'john@example.com',
    password: 'password123'
  })
});

const data = await response.json();
```

## Notas
- La contraseña se almacena hasheada usando bcrypt
- El email y username deben ser únicos en el sistema
- Después del registro, el usuario puede iniciar sesión usando el endpoint `/login`
