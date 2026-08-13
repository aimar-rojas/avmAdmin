# AVM Admin Android - Agent Instructions

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Hilt
- Retrofit
- Room
- DataStore
- WorkManager
- Navigation Compose

## Comandos

Abrir normalmente en Android Studio como proyecto Gradle.

Cuando sea posible antes de cerrar cambios, ejecutar las verificaciones Gradle aplicables. Preferir comandos específicos y acotados al cambio.

```bash
./gradlew :app:assembleDevDebug
./gradlew :app:testDevDebugUnitTest
```

## API

- Fuente de verdad backend: `https://api.productosaimar.com/api/`
- Mantener `dev` y `prod` apuntando a producción salvo instrucción explícita.
- Si se necesita backend local, cambiarlo manualmente de forma temporal.
- Retrofit usa rutas relativas tipo `v1/login`, `v1/trades`, etc.; por eso `BASE_URL` debe terminar en `/api/`.

## Arquitectura

- Mantener estrictamente la arquitectura feature-based actual: `features/trades`, `features/shipments`, `features/parties`, etc.
- Combinar feature-based con Clean Architecture: separar `presentation`, `domain` y `data` cuando el feature lo requiera.
- Mantener módulos compartidos como `core/di`, `core/network` y equivalentes.
- No cambiar la estructura actual sin una razón técnica clara.
- Las llamadas API deben pasar siempre por Repository. No llamar servicios Retrofit directamente desde ViewModel.
- Reutilizar componentes Compose existentes antes de crear nuevos. Si no existe un componente reutilizable adecuado, crearlo en el lugar compartido o feature correspondiente.

## Estado UI

- Usar MVVM o MVI según complejidad.
- Para flujos simples, usar ViewModel + `StateFlow` + `UiState`.
- Para estados complejos, múltiples eventos, estados derivados o flujo unidireccional claro, usar MVI.
- Cada pantalla debe exponer un único `UiState<T>` sellado con `Loading`, `Success`, `Empty`, `Error`.
- No usar flags booleanos sueltos para loading/error/empty.
- `Empty` y `Error` son estados distintos.
- Todo `Error` debe incluir acción visible de retry, como botón o pull-to-refresh.
- Loading inicial usa skeleton/shimmer a pantalla completa.
- Loading de refresco no debe ocultar contenido ya cargado.
- Los composables deben hacer `when(state)` y no contener lógica de negocio.

## UI / UX

- Usar siempre Jetpack Compose y Material 3.
- Evitar completamente XML.
- Respetar el design system existente: colores, tipografías, spacing, shapes, componentes y patrones visuales.
- Preferir bottom sheets para opciones, acciones y flujos secundarios.
- Evitar `AlertDialog` para opciones comunes; usarlo solo cuando tenga sentido por criticidad, bloqueo o confirmación destructiva.
- Aplicar buenas prácticas de componentización, reutilización y consistencia visual.
- Usar Navigation Compose para navegación.

## Dependencias

- Se pueden agregar librerías Android cuando exista una razón técnica clara.
- Antes de agregar una dependencia, revisar si el stack actual ya cubre el caso.

## Convenciones

- Código en inglés.
- Comentarios y documentación en español.
- Commits en inglés.
- Rama estándar: `master`.
