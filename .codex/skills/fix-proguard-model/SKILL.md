---
name: fix-proguard-model
description: Diagnostica y arregla errores de release build Android causados por R8/ProGuard eliminando u ofuscando modelos de datos, DTOs o data classes usados por serialización/reflection. Usar cuando assembleRelease falla, R8 reporta reglas faltantes, o una build release crashea con JsonSyntaxException, campos null inesperados, NoSuchFieldException, ClassNotFoundException, problemas de Gson/Retrofit/Room/reflection, o bugs que no ocurren en debug.
---

# Fix ProGuard Model

## Workflow

1. Reproducir o leer el fallo de release.
   - Buscar stacktrace completo, salida de R8, crash log o error de parsing.
   - Confirmar si ocurre solo en release/minified y no en debug.
   - Identificar la clase, paquete, endpoint, DTO, entity o adapter involucrado.

2. Ubicar el modelo afectado.
   - Buscar data classes bajo `app/src/main/java`.
   - Revisar servicios Retrofit, repositories y mappers que usan el modelo.
   - Si el error menciona JSON, revisar nombres de campos, `@SerializedName`, Gson y modelos anidados.
   - Si el error menciona reflection, revisar si la librería requiere nombres o constructores preservados.

3. Revisar reglas existentes.
   - Leer `app/proguard-rules.pro`.
   - Verificar si ya hay reglas para el paquete, Gson, Retrofit, Room, Hilt o modelos del feature.
   - Evitar duplicar reglas equivalentes.

4. Elegir el arreglo mínimo.
   - Preferir `@Keep` para un caso puntual o clase aislada.
   - Preferir `-keep` por paquete cuando varios DTOs/modelos del mismo feature comparten el problema.
   - No desactivar minificación completa salvo instrucción explícita.
   - No agregar reglas amplias como mantener toda la app si una regla específica resuelve el caso.

5. Aplicar el cambio.
   - Para DTOs/modelos usados por Gson/Retrofit, preservar clases y miembros serializados.
   - Para clases usadas por reflection, preservar constructores/campos/métodos requeridos.
   - Mantener comentarios en español si agregas una explicación en `proguard-rules.pro`.

6. Validar.
   - Ejecutar `./gradlew :app:assembleRelease` o el bundle/flavor equivalente.
   - Si el proyecto usa flavors y el fallo es de dev/prod, ejecutar el variant específico.
   - Si es posible, correr la app localmente en release y verificar que el parsing/flujo ya no falla.

## AVM Android Rules

- Mantener `dev` y `prod` apuntando a `https://api.productosaimar.com/api/` salvo instrucción explícita.
- No cambiar modelos, contratos API o estructura de features para esconder un problema de ProGuard.
- No hacer cambios destructivos o amplios sin confirmar.
- Código en inglés; comentarios y documentación en español.

## Common Patterns

Para modelos Gson/Retrofit en un paquete específico:

```proguard
# Mantener modelos serializados por Gson/Retrofit.
-keep class aimar.rojas.avmadmin.features.example.data.** { *; }
```

Para una clase puntual:

```kotlin
import androidx.annotation.Keep

@Keep
data class ExampleDto(...)
```

Usar estos patrones como punto de partida, adaptándolos al paquete real encontrado en el fallo.
