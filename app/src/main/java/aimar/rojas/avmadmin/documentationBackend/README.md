# Documentación de la API

Esta carpeta contiene la documentación completa de todos los endpoints de la API.

## Estructura

La documentación está organizada por features (características):

```
documentation/
├── README.md (este archivo)
└── features/
    ├── auth/          # Endpoints de autenticación
    │   ├── register.md
    │   └── login.md
    ├── user/          # Endpoints de usuario
    │   ├── get-profile.md
    │   ├── update-profile.md
    │   └── update-user.md
    └── trade/         # Endpoints de trades (compras y ventas)
        ├── get-trades.md
        ├── create-trade.md
        └── update-trade.md
```

## Base URL

```
http://localhost:5001/api/v1
```

En producción, reemplaza `localhost:5001` con la URL de tu servidor.

## Autenticación

La mayoría de los endpoints requieren autenticación mediante JWT Bearer Token.

### Cómo obtener el token

1. Registra un usuario con `POST /api/v1/register`
2. Inicia sesión con `POST /api/v1/login`
3. El token se retorna en la respuesta del login
4. Usa el token en el header `Authorization`:

```
Authorization: Bearer <tu_token_aqui>
```

### Endpoints Públicos (sin autenticación)

- `POST /api/v1/register` - Registro de usuario
- `POST /api/v1/login` - Inicio de sesión

### Endpoints Protegidos (requieren autenticación)

- `GET /api/v1/profile` - Obtener perfil de Boss
- `PUT /api/v1/profile` - Actualizar perfil de Boss
- `PUT /api/v1/user` - Actualizar usuario
- `GET /api/v1/trades` - Obtener trades
- `POST /api/v1/trades` - Crear trade
- `PUT /api/v1/trades/:id` - Actualizar trade
- `GET /api/v1/shipments` - Obtener shipments
- `POST /api/v1/shipments` - Crear shipment
- `PUT /api/v1/shipments/:id` - Actualizar shipment
- `GET /api/v1/parties` - Obtener parties
- `POST /api/v1/parties` - Crear party
- `PUT /api/v1/parties/:id` - Actualizar party
- `GET /api/v1/selections` - Obtener selections by trade
- `POST /api/v1/selections` - Crear selection
- `PUT /api/v1/selections/:id` - Actualizar selection

## Features

### Auth (Autenticación)
- [Register](./features/auth/register.md) - Registro de nuevos usuarios
- [Login](./features/auth/login.md) - Inicio de sesión y obtención de token

### User (Usuario)
- [Get Profile](./features/user/get-profile.md) - Obtener datos del Boss del usuario
- [Update Profile](./features/user/update-profile.md) - Actualizar datos del Boss
- [Update User](./features/user/update-user.md) - Actualizar datos del usuario

### Trade (Trades - Compras y Ventas)
- [Get Trades](./features/trade/get-trades.md) - Obtener todos los trades con paginación y filtros
- [Create Trade](./features/trade/create-trade.md) - Crear un nuevo trade (compra o venta)
- [Update Trade](./features/trade/update-trade.md) - Actualizar un trade existente

### Shipment (Shipments - Envíos)
- [Get Shipments](./features/shipment/get-shipments.md) - Obtener todos los shipments con paginación y filtros
- [Create Shipment](./features/shipment/create-shipment.md) - Crear un nuevo shipment
- [Update Shipment](./features/shipment/update-shipment.md) - Actualizar un shipment existente

### Party (Parties - Proveedores y Compradores)
- [Get Parties](./features/party/get-parties.md) - Obtener todos los parties con filtros (sin paginación)
- [Create Party](./features/party/create-party.md) - Crear un nuevo party
- [Update Party](./features/party/update-party.md) - Actualizar un party existente

### Selection by Trade (Selecciones por Trade)
- [Get Selections](./features/selection_by_trade/get-selections.md) - Obtener todas las selecciones con filtros (sin paginación)
- [Create Selection](./features/selection_by_trade/create-selection.md) - Crear una nueva selección
- [Update Selection](./features/selection_by_trade/update-selection.md) - Actualizar una selección existente

## Tipos de Trade

Los trades pueden ser de dos tipos:

- **PURCHASE**: Compra (adquisición de productos)
- **SALE**: Venta (venta de productos)

El endpoint `GET /api/v1/trades` permite filtrar por tipo usando el parámetro `trade_type`.

## Formato de Fechas

Las fechas deben estar en formato **RFC3339** (ISO 8601):

```
2024-01-01T10:00:00Z
2024-01-01T10:00:00-05:00
```

## Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Error en la solicitud (validación fallida)
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: No tienes permiso para realizar esta acción
- `404 Not Found`: Recurso no encontrado
- `500 Internal Server Error`: Error interno del servidor

## Ejemplos de Respuestas de Error

Todas las respuestas de error siguen este formato:

```json
{
  "error": "mensaje de error descriptivo"
}
```

## Paginación

Los endpoints que soportan paginación usan los parámetros:

- `page`: Número de página (por defecto: 1)
- `limit`: Registros por página (por defecto: 50, máximo: 200)

La respuesta incluye metadatos de paginación:

```json
{
  "total": 150,
  "page": 1,
  "limit": 50,
  "total_pages": 3,
  "has_next": true,
  "has_previous": false
}
```

## Notas Importantes

1. **Trades**: El endpoint `GET /api/v1/trades` trae **TODOS** los trades del sistema, no solo los del usuario autenticado. El usuario solo se usa para autenticación.

2. **Perfil de Boss**: El usuario debe tener datos de Boss completados (`isCompletedProfile: true` en el login) para poder crear trades.

3. **Actualización Parcial**: Los endpoints de actualización (`PUT`) permiten actualizar solo los campos que envíes. No es necesario enviar todos los campos.

4. **Relaciones**: Los endpoints de trades incluyen automáticamente las relaciones (Party, Boss, Shipment) en las respuestas.
