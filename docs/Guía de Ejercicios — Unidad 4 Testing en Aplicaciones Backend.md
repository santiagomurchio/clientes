# Guía de Ejercicios — Unidad 4: Testing en Aplicaciones Backend

Esta guía propone una progresión de ejercicios para la Unidad 4, yendo de anotaciones básicas de JUnit 5 hasta mocking con Mockito y MockServer. Está adaptada para resolverse directamente sobre **este proyecto** (`com.fube.clientes`, un CRUD de clientes con Spring Boot). Se asume que el proyecto **no tiene ningún test escrito todavía** — cada nivel indica exactamente qué archivo de test crear y dónde.

**Progresión general:** JUnit básico → TDD → Mockito → persistencia con H2/@DataJpaTest → @SpringBootTest → MockServer (desafío opcional) → cierre teórico sobre pirámide de tests y cobertura con JaCoCo.

**Nota:** el batch de Spring Batch (paquete `batch`) queda fuera de esta guía — se comenta en clase aparte, testearlo agrega complejidad (JobLauncherTestUtils, contexto de batch) que no aporta a los objetivos de esta unidad.

**Nota sobre el controlador:** al momento de escribir esta guía, `ClienteControlador` tiene wireado con `@GetMapping`/`@PostMapping` solo los endpoints `GET /clientes/{id}`, `GET /clientes` y `POST /clientes`. Los métodos `crear(...)`, `actualizarCliente(...)` y `eliminar(...)` existen pero no tienen anotación de mapping todavía, así que no son endpoints reales — no se testean por MockMvc en esta guía. Si tu profesor te pidió completarlos, podés extender el Nivel 5 vos mismo una vez que los tengas andando.

## Nivel 1 — Fundamentos de JUnit 5

**Objetivo:** familiarizarse con el ciclo de vida de un test y las anotaciones básicas: `@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@AfterAll`, y las assertions de `Assertions` (`assertEquals`, `assertTrue`, `assertThrows`, etc.).

**Clase a testear (sin Spring, sin base de datos):** `com.fube.clientes.mapper.ClienteMapper` — es una clase con métodos estáticos (`toDTO`, `toEntity`), ideal para arrancar sin necesidad de levantar contexto de Spring.

Crear el archivo `src/test/java/com/fube/clientes/mapper/ClienteMapperTest.java`.

### Ejercicio 1.1 — Primer test

Escribir un test que construya un `Cliente` con `Cliente.builder()...build()` (todos los campos completos) y verifique que `ClienteMapper.toDTO(cliente)` devuelve un `ClienteDTO` con el mismo `id`, `nombre`, `apellido`, `email`, `telefono` y `direccion`. Usar `assertEquals` para cada campo (o `assertAll` agrupándolos).

### Ejercicio 1.2 — Casos borde

Agregar 2-3 tests más para `toEntity(dto)`: un DTO con todos los campos, y un DTO con algunos campos en `null` (`telefono`, `direccion`). Verificar que el `Cliente` resultante preserva esos nulls tal cual. Cada test con **un solo objetivo** y nombre descriptivo (`deberiaMapearTelefonoNuloComoNulo`).

### Ejercicio 1.3 — Ciclo de vida

Agregar un campo `Cliente clienteDeEjemplo` a la clase de test. Inicializarlo en un método `@BeforeEach` (en lugar de construirlo en cada test) con datos fijos que reutilicen varios tests. Agregar un `@AfterEach` que solo imprima por consola `"Test finalizado"`, para ver en qué orden se ejecuta todo.

### Ejercicio 1.4 — Setup y cierre únicos

Agregar un método estático `@BeforeAll` (por ejemplo, para loguear `"Iniciando ClienteMapperTest"`) y uno `@AfterAll`. Discutir en clase: ¿cuántas veces se ejecuta cada uno si la clase tiene 5 tests? ¿Y `@BeforeEach`?

### Ejercicio 1.5 — Comportamiento actual con nulls

Escribir un test que llame a `ClienteMapper.toDTO(null)` y observar qué pasa (spoiler: hoy explota, porque el builder llama a los getters del `cliente` sin chequear null). Documentar ese comportamiento con `assertThrows(NullPointerException.class, () -> ClienteMapper.toDTO(null))`. Sirve para discutir: ¿este test "protege" al código o solo documenta un bug latente? ¿Debería `toDTO` manejar el `null` de otra forma?

**Recursos de este nivel:**

- [JUnit & Mockito Tutorial (Stackabyte)](https://stackabyte.com/tutorials/java/junit-mockito-tutorial) — panorama general de anotaciones de JUnit 5, buen punto de partida antes del ejercicio 1.1.

## Nivel 2 — TDD (Test-Driven Development)

**Objetivo:** experimentar el ciclo **Red → Green → Refactor** escribiendo el test antes que el código de producción.

**Contexto:** el proyecto todavía no tiene una clase de validación de negocio para `Cliente`. La vamos a crear desde cero, con TDD, en un paquete nuevo: `com.fube.clientes.validacion.ClienteValidador`.

### Ejercicio 2.1 — Guiado, paso a paso

Implementar `ClienteValidador.esEmailValido(String email)` (método estático), que devuelve `true` si el string tiene forma de email razonable (contiene `@`, tiene texto antes y después, tiene un `.` en el dominio) y `false` en cualquier otro caso, incluyendo `null` o `""`. Seguir el ciclo explícitamente y documentarlo (en un comentario corto o un README):

1. **Red** — escribir `deberiaAceptarEmailValido()` esperando `true` para `"juan@mail.com"`, sin que exista aún `ClienteValidador` (o con el método devolviendo siempre `false`) → el test falla.
2. **Green** — escribir el mínimo código para que ese test pase (puede ser una solución "tramposa").
3. **Refactor** — generalizar sin romper el test.
4. Repetir el ciclo agregando los casos negativos: sin `@`, vacío, `null`, sin dominio (`"juan@"`).

### Ejercicio 2.2 — Sin guía

Aplicar el mismo ciclo para implementar `ClienteValidador.tieneDatosCompletos(Cliente cliente)`, que devuelve `true` solo si `telefono` y `direccion` no son `null` (ni `""`). Esta vez sin pasos explícitos: se espera que documenten su propio ciclo red-green-refactor en el historial de commits (mínimo 3 commits: test que falla, implementación mínima, refactor).

> Dato para conectar con lo que ya existe en el proyecto: `ClienteRepositorio` ya tiene una query derivada `findByTelefonoIsNullOrDireccionIsNull()` que responde una pregunta parecida a nivel de base de datos. `tieneDatosCompletos` responde la misma pregunta a nivel de un objeto en memoria — buena excusa para discutir dónde conviene poner cada tipo de validación.

### Ejercicio 2.3 — Reflexión (para el coloquio)

Preguntas para responder por escrito: ¿en qué momento del ciclo se sintieron tentados a escribir más código del necesario para pasar el test? ¿Cambiaría el diseño de `ClienteValidador` si hubieran escrito el código antes que el test?

**Recursos de este nivel:**

- [TDD in Java (BrowserStack)](https://www.browserstack.com/guide/tdd-in-java) — introducción conceptual al ciclo red-green-refactor.
- [Test-Driven Development for a Shopping Cart (Baeldung)](https://www.baeldung.com/java-test-driven-list) — ejemplo completo y guiado, buena referencia para el ejercicio 2.1.

## Nivel 3 — Mockito: mockeo de dependencias

**Objetivo:** aislar la clase bajo test de sus dependencias reales usando `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, `when(...)` y `verify(...)`.

**Contexto:** `ClienteServicioImpl` depende de un único colaborador, `ClienteRepositorio`. Es un buen caso simple para practicar mocking antes de escenarios con varias dependencias.

Crear el archivo `src/test/java/com/fube/clientes/servicios/ClienteServicioImplTest.java`.

### Ejercicio 3.1 — Primer mock

Anotar la clase con `@ExtendWith(MockitoExtension.class)`. Declarar `@Mock private ClienteRepositorio repositorio` e inyectarlo con `@InjectMocks private ClienteServicioImpl servicio`. Escribir un test que verifique que, si `repositorio.findById(id)` devuelve `Optional.empty()`, el método `buscarPorId(id)` lanza `RecursoNoEncontradoException`.

### Ejercicio 3.2 — Camino feliz

Testear `crear(cliente)`: mockear `repositorio.save(cliente)` para que devuelva el mismo `cliente` con un `id` asignado, y verificar que el `ClienteDTO` resultante tiene todos los campos correctamente mapeados (usa el `ClienteMapper` real — no se mockea).

### Ejercicio 3.3 — Verificación de interacciones

Agregar un test que use `verify(repositorio).save(cliente)` para confirmar que `crear(...)` efectivamente delega en el repositorio. Agregar otro para `eliminar(id)` que confirme `verify(repositorio).deleteById(id)`.

### Ejercicio 3.4 — Paginado

Testear `buscarTodos(pageable)`: mockear `repositorio.findAll(pageable)` para que devuelva un `PageImpl<Cliente>` con 2-3 clientes, y verificar que el `Page<ClienteDTO>` resultante tiene el mismo tamaño y los datos mapeados correctamente.

**Recursos de este nivel:**

- [Injecting Mockito Mocks into Spring Beans (Baeldung)](https://www.baeldung.com/injecting-mocks-in-spring) — explica `@Mock`/`@InjectMocks` en profundidad.
- [Unit Testing in Spring Boot Project using Mockito and JUnit (GeeksforGeeks)](https://www.geeksforgeeks.org/advance-java/unit-testing-in-spring-boot-project-using-mockito-and-junit/) — buen resumen visual del flujo completo.

## Nivel 4 — Testing de persistencia: @DataJpaTest + H2

**Objetivo:** testear `ClienteRepositorio` de forma aislada, usando la base en memoria H2 que se levanta automáticamente con `@DataJpaTest` (el proyecto ya trae `com.h2database:h2` como `testRuntimeOnly` en `build.gradle`).

Crear el archivo `src/test/java/com/fube/clientes/repositorio/ClienteRepositorioTest.java`.

### Ejercicio 4.1 — Primer test de repositorio

Anotar la clase con `@DataJpaTest`. Usar `TestEntityManager` para persistir un `Cliente` de prueba (`entityManager.persistAndFlush(...)`) y verificar con `repositorio.findById(id)` que se lo puede recuperar con los mismos datos.

### Ejercicio 4.2 — Query derivada ya existente

Testear `findByTelefonoIsNullOrDireccionIsNull()`: insertar 3 clientes (uno con todos los datos, uno sin `telefono`, uno sin `direccion`), y verificar que el método devuelve exactamente los dos incompletos.

### Ejercicio 4.3 — Constraint de unicidad

`Cliente.email` está mapeado como `@Column(nullable = false, unique = true)`. Escribir un test que persista un cliente, luego intente persistir otro con el mismo email, y verifique que se lanza `DataIntegrityViolationException` al hacer `flush()`.

### Ejercicio 4.4 — Discusión (sin código)

El proyecto usa PostgreSQL en producción (ver `spring.datasource.url` en `application.properties`) con migraciones de Flyway (`src/main/resources/db/migration`), pero los tests de este nivel corren contra H2 con `spring.jpa.hibernate.ddl-auto=create-drop` y `spring.flyway.enabled=false` (ver `src/test/resources/application.properties`). ¿Qué tipo de errores podrían **no** detectarse con este enfoque, considerando que el schema real lo define Flyway y no Hibernate? (Introduce la idea de Testcontainers como alternativa, sin necesidad de implementarla en este nivel.)

**Recursos de este nivel:**

- [Spring Boot with H2 Database (HowToDoInJava)](https://howtodoinjava.com/spring-boot/h2-database-example/) — configuración de H2 en Spring Boot.
- [Spring Boot Testing (HowToDoInJava)](https://howtodoinjava.com/spring-boot/junit-with-spring-boot/) — panorama de las distintas anotaciones de testing en Spring Boot, incluye `@DataJpaTest`.

## Nivel 5 — @SpringBootTest e integración completa

**Objetivo:** entender la diferencia entre testear componentes aislados (niveles anteriores) y levantar el contexto completo de la aplicación para un test de integración real, usando `MockMvc` para simular requests HTTP sin necesidad de un servidor corriendo.

Crear el archivo `src/test/java/com/fube/clientes/controladores/ClienteControladorTest.java`.

### Ejercicio 5.1 — Contexto completo

Crear un test anotado con `@SpringBootTest` que simplemente verifique que el contexto de Spring levanta sin errores (`contextLoads()`, sin body). Sirve para ver cuánto más tarda en ejecutarse comparado con los tests de niveles anteriores.

### Ejercicio 5.2 — MockMvc sobre los endpoints reales

Con `@SpringBootTest` + `@AutoConfigureMockMvc`, testear:

- `GET /clientes/{id}` con un cliente existente (insertarlo antes vía `ClienteRepositorio` autowireado) → status 200 y body con los datos esperados.
- `GET /clientes/{id}` con un id inexistente → status 404.
- `POST /clientes` con un `ClienteDTO` válido en el body → status 204 (`crearCliente` responde `@ResponseStatus(HttpStatus.NO_CONTENT)`), y verificar aparte que el cliente quedó persistido consultando el repositorio.

### Ejercicio 5.3 — Integración completa del manejo de errores

Testear que el 404 de `GET /clientes/{id}` (id inexistente) devuelve un body con la forma de `ErrorResponse` (`status`, `error`, `mensaje`) tal como lo arma `ManejadorGlobalExcepciones`, usando `jsonPath(...)` para verificar los campos del JSON. Esto integra controller → service → exception handler en un solo test.

### Ejercicio 5.4 — Discusión: ¿cuándo usar cada tipo?

Comparar en una tabla propia (a completar) cuándo conviene un test con Mockito puro (Nivel 3), uno con `@DataJpaTest` (Nivel 4), y uno con `@SpringBootTest` (este nivel) — en términos de velocidad, qué cubre y qué NO cubre cada uno. Esto prepara el terreno para el Nivel 7 (pirámide de tests).

**Recursos de este nivel:**

- [Spring Boot Testing (HowToDoInJava)](https://howtodoinjava.com/spring-boot/junit-with-spring-boot/) — ya usado en el Nivel 4, retomar la sección específica de `@SpringBootTest` y `MockMvc`.

## Nivel 6 (opcional / desafío) — MockServer: simulación de un servicio externo

**Objetivo:** dar el paso de mockear a nivel de código Java (Mockito, Nivel 3) a simular la comunicación **HTTP real** hacia un servicio externo.

**Contexto:** a diferencia de un sistema de microservicios, `servicio-clientes` hoy **no le pega a ningún servicio externo por HTTP** — es un monolito. Para practicar MockServer con sentido, este nivel pide primero agregar una integración chica y después testearla. Es opcional: si les queda ajustado el tiempo, sáltenlo sin culpa.

### Ejercicio 6.1 — Agregar el cliente HTTP

Crear `com.fube.clientes.cliente.ValidadorEmailExternoClient`, una clase chica que use `RestTemplate` (o `WebClient`) para pegarle a una URL configurable (ej: `http://localhost:{puerto}/validar-email?email=...`) y devolver `true`/`false` según la respuesta. No hace falta un servicio real: en este ejercicio el "servicio externo" lo va a simular MockServer en el test.

### Ejercicio 6.2 — Primer stub

Levantar una instancia de MockServer en el test (`ClientAndServer`), apuntar el `RestTemplate` del cliente al puerto de MockServer, y configurar un stub: cuando llega `GET /validar-email?email=juan@mail.com`, responder `200` con `{"valido": true}`. Verificar que `ValidadorEmailExternoClient.esValido(email)` devuelve `true`.

### Ejercicio 6.3 — Simular un error

Configurar el stub para que devuelva `500` o timeout, y decidir (e implementar) cómo debería comportarse el cliente ante ese caso: ¿propaga la excepción? ¿devuelve `false` por defecto (fail-open)? ¿fail-closed? Testear la decisión tomada.

### Ejercicio 6.4 — Verificar la request enviada

Usar las capacidades de verificación de MockServer para confirmar que `ValidadorEmailExternoClient` efectivamente envió la request esperada (método, path, y el email como query param).

**Recursos de este nivel:**

- [Testing With MockServer (Baeldung)](https://www.baeldung.com/mockserver) — tutorial completo con ejemplos de stubs y verificación.
- [MockServer — sitio oficial](https://www.mock-server.com/) — documentación de referencia.

## Nivel 7 (cierre) — Pirámide de tests y cobertura con JaCoCo

**Objetivo:** cerrar la unidad con una mirada más teórica/reflexiva, conectando todo lo practicado con el concepto de estrategia de testing y su medición.

**Contexto:** el proyecto ya trae el plugin de JaCoCo configurado en `build.gradle`, con un mínimo de cobertura (`jacocoTestCoverageVerification`) fijado en `0.15` y una nota que dice *"Cobertura actual ~20%. Ir subiendo este mínimo a medida que se agreguen tests."* — este nivel usa esa configuración tal cual está.

### Ejercicio 7.1 — Clasificar los propios tests

Armar una tabla propia clasificando cada test que escribieron en los niveles 1 a 5 según el nivel de la pirámide al que pertenece (unitario, integración), y qué porcentaje aproximado del total representa cada nivel. Discutir: ¿la proporción se parece a una pirámide, o a otra forma (rombo, reloj de arena)?

### Ejercicio 7.2 — Correr y leer el reporte de JaCoCo

Ejecutar `./gradlew test jacocoTestReport` y abrir `build/reports/jacoco/test/html/index.html`. Identificar: ¿qué clases tienen menor cobertura? ¿Por qué (son las que no testearon todavía, como `batch` o `ClienteBatchConfig`, que quedaron fuera de esta guía)? Después correr `./gradlew jacocoTestCoverageVerification` y ver si pasa con el mínimo actual de `0.15`. Como desafío, subir ese mínimo en `build.gradle` a un valor acorde a lo que lograron y volver a correrlo.

### Ejercicio 7.3 — Discusión crítica (sin código)

¿Por qué un 100% de cobertura no garantiza que el código esté bien testeado? Dar un ejemplo concreto usando alguno de los tests ya escritos (por ejemplo, un test que llama al método pero no verifica el valor devuelto, solo que "no explotó").

### Ejercicio 7.4 — Caso Lombok

`Cliente` y `ClienteDTO` usan `@Data`/`@Builder`/`@NoArgsConstructor`/`@AllArgsConstructor` de Lombok. Observar en el reporte de JaCoCo cómo esas clases aparecen con cobertura parcial (getters/setters/equals/hashCode generados que nunca se llaman "a mano"). Investigar y aplicar una exclusión en la configuración de `jacocoTestReport`/`jacocoTestCoverageVerification` en `build.gradle` (por `classDirectories` con un filtro de exclusión, o generando la anotación `@Generated` de Lombok vía `lombok.config` y excluyendo por anotación) para que esas clases no cuenten en el cálculo.

**Recursos de este nivel:**

- [The Test Pyramid in Practice (Baeldung)](https://www.baeldung.com/spring-test-pyramid-practical-example) — explica la pirámide con ejemplos concretos en Spring, buen cierre conceptual.
- [Cobertura en un proyecto Maven multimódulo con JaCoCo (Adictos al Trabajo)](https://adictosaltrabajo.com/2022/10/10/cobertura-en-un-proyecto-maven-multimodulo-con-jacoco/) — el proyecto es de un solo módulo, pero sirve para ver más ejemplos de configuración de exclusiones de JaCoCo.

## Recursos — Resumen por nivel

| Nivel | Tema | Enlace |
| --- | --- | --- |
| 1 | JUnit 5 básico | [JUnit & Mockito Tutorial (Stackabyte)](https://stackabyte.com/tutorials/java/junit-mockito-tutorial) |
| 2 | TDD | [TDD in Java (BrowserStack)](https://www.browserstack.com/guide/tdd-in-java) |
| 2 | TDD | [TDD for a Shopping Cart (Baeldung)](https://www.baeldung.com/java-test-driven-list) |
| 3 | Mockito | [Injecting Mocks in Spring (Baeldung)](https://www.baeldung.com/injecting-mocks-in-spring) |
| 3 | Mockito | [Unit Testing con Mockito y JUnit (GeeksforGeeks)](https://www.geeksforgeeks.org/advance-java/unit-testing-in-spring-boot-project-using-mockito-and-junit/) |
| 4 | H2 / @DataJpaTest | [Spring Boot with H2 (HowToDoInJava)](https://howtodoinjava.com/spring-boot/h2-database-example/) |
| 4 y 5 | Testing en Spring Boot | [Spring Boot Testing (HowToDoInJava)](https://howtodoinjava.com/spring-boot/junit-with-spring-boot/) |
| 6 | MockServer | [Testing With MockServer (Baeldung)](https://www.baeldung.com/mockserver) |
| 6 | MockServer | [MockServer — sitio oficial](https://www.mock-server.com/) |
| 7 | Pirámide de tests | [The Test Pyramid in Practice (Baeldung)](https://www.baeldung.com/spring-test-pyramid-practical-example) |
| 7 | Cobertura (JaCoCo) | [Cobertura multimódulo con JaCoCo (Adictos al Trabajo)](https://adictosaltrabajo.com/2022/10/10/cobertura-en-un-proyecto-maven-multimodulo-con-jacoco/) |
