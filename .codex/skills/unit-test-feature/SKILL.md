---
name: unit-test-feature
description: Crea o mejora tests unitarios para una feature Android de AVM, incluyendo ViewModel, UseCase, Repository, mappers y estados UiState/MVI. Usar cuando el usuario pida agregar, generar o arreglar tests para una feature, clase, pantalla o flujo específico; cuando falten pruebas de success/error/empty; o cuando haya que instalar dependencias de testing como Mockito-Kotlin, kotlinx-coroutines-test o Turbine.
---

# Unit Test Feature

## Workflow

1. Entender el objetivo.
   - Identificar feature, clase o pantalla solicitada.
   - Leer la implementación antes de escribir tests.
   - Ubicar dependencias externas: repositories, API services, DAO, DataStore, use cases, dispatchers.

2. Revisar infraestructura de testing.
   - Leer `app/build.gradle.kts` y `gradle/libs.versions.toml`.
   - Confirmar JUnit, Mockito/Mockito-Kotlin, kotlinx-coroutines-test y Turbine si se testea `Flow`.
   - Agregar dependencias faltantes solo si son necesarias para el test solicitado.

3. Elegir el alcance.
   - ViewModel: validar transiciones de `UiState`, eventos, retry y refresh.
   - UseCase: validar reglas de negocio y errores.
   - Repository: mockear API/DAO y validar mappers, errores y edge cases.
   - Mapper: probar conversión completa, nullability y valores por defecto.

4. Escribir tests.
   - Seguir Given-When-Then o Arrange-Act-Assert.
   - Nombrar tests como `method_condition_expectedResult()`.
   - Cubrir al menos success, error y empty/edge case cuando aplique.
   - No llamar API real ni depender de red.
   - No cambiar código de producción para que el test pase salvo que revele un bug real.

5. Validar.
   - Ejecutar el test más específico primero.
   - Luego ejecutar `./gradlew :app:testDevDebugUnitTest` o el variant equivalente.
   - Si falla, ajustar mocks/asserts o reportar bug real con evidencia.

## Subagents

Usar un subagente solo cuando aporte paralelismo real, por ejemplo:

- Explorar una feature grande y listar clases/dependencias candidatas a test.
- Revisar patrones de tests existentes en el repo.
- Hacer una segunda lectura independiente de casos faltantes.

El agente principal debe conservar el control de:

- Qué archivos se modifican.
- Qué dependencias se agregan.
- El diseño final de tests.
- La ejecución de Gradle y el resumen final.

No delegar al subagente cambios directos en el repo si puede producir conflictos o tocar demasiado.

## AVM Android Rules

- Mantener feature-based + Clean Architecture.
- Las llamadas API pasan por Repository; no mockear Retrofit desde ViewModel si el ViewModel depende de Repository.
- Usar `StateFlow`/`UiState` para MVVM simple y MVI cuando el flujo sea complejo.
- Cada pantalla debe modelar `Loading`, `Success`, `Empty`, `Error` como estados distintos.
- Todo error testeable debe contemplar retry cuando exista UI/acción asociada.
- Reutilizar test utilities existentes antes de crear nuevas.
- Código en inglés; nombres de tests en inglés; comentarios y documentación en español.

## Preferred Test Shape

```kotlin
@Test
fun loadItems_repositoryReturnsEmpty_emitsEmptyState() = runTest {
    // Given

    // When

    // Then
}
```

Mantener mocks pequeños, assertions claras y tests independientes entre sí.
