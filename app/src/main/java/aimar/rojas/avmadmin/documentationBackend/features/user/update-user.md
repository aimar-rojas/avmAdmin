# Update User - Actualizar Usuario

## Descripción
Endpoint para actualizar los datos del usuario autenticado (username y/o email).

## Endpoint
```
PUT /api/v1/user
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Request Body
```json
{
  "username": "nuevo_username",
  "email": "nuevo@email.com"
}
```

### Campos Opcionales
- `username` (string): Nuevo nombre de usuario. Mínimo 3 caracteres, máximo 50 caracteres. Debe ser único.
- `email` (string): Nuevo correo electrónico. Debe ser válido y único.

**Nota:** Puedes actualizar solo uno de los campos o ambos. Solo se actualizarán los campos que envíes.

## Response

### Success (200 OK)
```json
{
  "message": "Usuario actualizado exitosamente",
  "user": {
    "id": 1,
    "username": "nuevo_username",
    "email": "nuevo@email.com",
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-26T10:30:00Z"
  }
}
```

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "el email ya está en uso"
}
```

```json
{
  "error": "el username ya está en uso"
}
```

## Ejemplo de Uso

### Actualizar solo username
```bash
curl -X PUT http://localhost:5001/api/v1/user \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevo_username"
  }'
```

### Actualizar solo email
```bash
curl -X PUT http://localhost:5001/api/v1/user \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nuevo@email.com"
  }'
```

### Actualizar ambos campos
```bash
curl -X PUT http://localhost:5001/api/v1/user \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevo_username",
    "email": "nuevo@email.com"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:5001/api/v1/user', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    username: 'nuevo_username',
    email: 'nuevo@email.com'
  })
});

const data = await response.json();
```

## Notas
- La actualización es parcial: solo se actualizan los campos que envíes
- El sistema valida que el email y username no estén en uso por otro usuario
- Puedes actualizar solo el campo que necesites cambiar
- La contraseña no se puede actualizar con este endpoint (requiere endpoint específico si se implementa)
