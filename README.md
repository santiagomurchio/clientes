# Clientes

CRUD de clientes desarrollado con Spring Boot.

## Tecnologías

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- Spring Batch
- Flyway
- Lombok
- PostgreSQL
- Springdoc OpenAPI (Swagger)
- Observabilidad: Logback + logstash-logback-encoder, Micrometer Tracing, Grafana Loki, Prometheus, Grafana

## Estructura del proyecto

```
com.fube.clientes
├── batch            # Job, Step, Reader, Processor y Writer de Spring Batch
├── controladores    # Capa REST
├── dto              # Objetos de transferencia de datos
├── mapper           # Conversión entre entidad y DTO
├── modelos          # Entidades JPA
├── observabilidad   # Filtro que devuelve el traceId en el header X-Trace-Id
├── repositorio      # Acceso a datos (Spring Data JPA)
└── servicios        # Lógica de negocio
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/clientes` | Listar todos los clientes |
| `GET` | `/api/clientes/{id}` | Obtener cliente por ID (`404` si no existe) |
| `POST` | `/api/clientes` | Crear cliente (`201`) |
| `PUT` | `/api/clientes/{id}` | Actualizar cliente (`404` si no existe) |
| `DELETE` | `/api/clientes/{id}` | Eliminar cliente (`204`, o `404` si no existe) |
| `GET` | `/actuator/health` | Estado de la aplicación |

Todas las respuestas incluyen el header `X-Trace-Id` (ver [Observabilidad](#observabilidad)).

## Migraciones

Las migraciones se gestionan con Flyway y se ejecutan automáticamente al iniciar la aplicación.

| Versión | Descripción |
|---------|-------------|
| `V1` | Creación de la tabla `clientes` |
| `V2` | Seed de 92 clientes |

## Batch job

El job `fillClientesJob` recorre todos los clientes que tienen teléfono o domicilio nulo y les asigna un valor por defecto.

| Campo | Valor por defecto |
|-------|-------------------|
| `telefono` | `0000-0000` |
| `direccion` | `Sin domicilio` |

El job no se ejecuta automáticamente al iniciar la aplicación (`spring.batch.job.enabled=false`).

## Cómo correr el proyecto

```bash
./gradlew bootRun
```

Para mandar los logs a Loki, activá el perfil `loki` (ver [Observabilidad](#observabilidad)):

```bash
./gradlew bootRun --args='--spring.profiles.active=loki'
```

En IntelliJ: *Run Configuration → Active profiles: `loki`*.

## Documentación API

Con la aplicación corriendo, accedé a la UI de Swagger en:

```
http://localhost:8080/swagger-ui.html
```

## Base de datos con Docker

Levantá un contenedor de PostgreSQL con las credenciales que usa la aplicación:

```bash
docker run --name clientes-db \
  -e POSTGRES_DB=clientesdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -v clientes-db-data:/var/lib/postgresql/data \
  --restart unless-stopped \
  -d postgres:16
```

| Parámetro | Valor |
|-----------|-------|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `clientesdb` |
| Usuario | `postgres` |
| Contraseña | `postgres` |

## Observabilidad

### Conceptos

**Observabilidad** es poder entender qué está pasando adentro de un sistema a partir de lo que emite hacia afuera, sin agregar código nuevo ni conectarse a debuggear. Se apoya en tres tipos de señales, los *tres pilares*:

| Señal | Qué responde | En este proyecto |
|-------|--------------|------------------|
| **Logs** | ¿Qué pasó, exactamente? | Logback → archivo JSON y Grafana Loki |
| **Métricas** | ¿Cuánto, qué tan seguido, qué tan rápido? | Micrometer → `/actuator/prometheus` → Prometheus |
| **Trazas** | ¿Por dónde pasó una request y cuánto tardó cada parte? | Micrometer Tracing genera el `traceId` / `spanId` |

Otros conceptos importantes:

- **Logging estructurado:** cada log es un JSON con campos (`level`, `traceId`, `clienteId`, etc.) en vez de una línea de texto libre. Así se puede filtrar y agregar como si fuera una base de datos.
- **Correlación con `traceId`:** cada request HTTP recibe un id único que aparece en *todas* las líneas de log que genera. Con ese id se reconstruye la historia completa de una request, aunque esté mezclada con miles de otras.
- **MDC (Mapped Diagnostic Context):** es el contexto del thread actual que Logback agrega a cada log. Micrometer Tracing pone ahí el `traceId` y el `spanId`.
- **Labels vs. contenido (en Loki):** los labels indexan los streams y tienen que ser pocos y de **baja cardinalidad** (`app`, `level`, `host`). Los valores que cambian mucho (`traceId`, `clienteId`) van dentro del JSON y se filtran en la consulta.
- **Niveles de log:** `ERROR` (algo falló y hay que actuar), `WARN` (algo raro pero manejado, como un cliente inexistente), `INFO` (eventos de negocio), `DEBUG` (detalle para desarrollo).
- **No loguear datos sensibles:** nunca email, teléfono, DNI, contraseñas ni tokens. Se loguea el `clienteId`, no el cliente.

### Arquitectura

```
                    App Spring Boot (localhost:8080)
                    ┌────────────────────────────────────────────┐
  Postman ──HTTP──► │ Controlador → Servicio → Repositorio ──────┼──► PostgreSQL :5432
     ▲              │      │ logs con traceId (MDC)               │    (contenedor clientes-db)
     │ X-Trace-Id   │      ▼                                     │
     └──────────────┤ Logback                                    │
                    │  ├── CONSOLE   → texto legible             │
                    │  ├── JSON_FILE → logs/clientes.json        │
                    │  └── LOKI      → push HTTP (perfil loki) ──┼──► Loki :3100 ───────┐
                    │                                            │                      ├──► Grafana :3000
                    │ Micrometer → /actuator/prometheus  ◄───────┼─── Prometheus :9090 ─┘
                    └────────────────────────────────────────────┘    (pull cada 5 s)
                                                    Loki, Prometheus y Grafana: observabilidad/docker-compose.yml
```

**Logs vs. métricas, push vs. pull:** los logs los *empuja* la app hacia Loki. Las métricas no se envían: la app las publica en `/actuator/prometheus` y Prometheus *va a buscarlas* cada 5 segundos. Un log cuenta un evento con detalle; una métrica es un número que se agrega en el tiempo (cuántos, cada cuánto, cuánto tarda) y es mucho más barata de guardar.

| Pieza | Para qué |
|-------|----------|
| `src/main/resources/logback-spring.xml` | Define a dónde van los logs y en qué formato |
| `logstash-logback-encoder` | Escribe los logs en JSON; `kv("clienteId", id)` agrega campos propios |
| `micrometer-tracing-bridge-brave` + Actuator | Genera `traceId`/`spanId` por request y los pone en el MDC |
| `observabilidad/TraceIdResponseFilter` | Devuelve el `traceId` al cliente en el header `X-Trace-Id` |
| `loki-logback-appender` (loki4j) | Envía los logs a Loki por HTTP, en lotes y sin bloquear la app |
| `micrometer-registry-prometheus` | Publica las métricas en `/actuator/prometheus`, en el formato que lee Prometheus |
| `ClienteServicioImpl` (`MeterRegistry`) | Métricas de negocio propias: `clientes_creados_total`, `clientes_encontrados_total{operacion}` y `clientes_no_encontrados_total{operacion}` |
| Grafana Loki | Guarda los logs e indexa solo los labels |
| Prometheus | Lee las métricas de la app cada 5 s y las guarda como series de tiempo |
| Grafana | Consulta Loki (LogQL) y Prometheus (PromQL); datasources y dashboards vienen provisionados |

```
observabilidad/
├── docker-compose.yml                           # Loki + Prometheus + Grafana
├── prometheus/prometheus.yml                    # Qué endpoint leer y cada cuánto
└── grafana/
    ├── provisioning/datasources/datasources.yml # Datasources Loki y Prometheus, sin configurar nada a mano
    ├── provisioning/dashboards/dashboards.yml   # Le dice a Grafana dónde buscar dashboards
    ├── dashboards/clientes-logs.json            # Dashboard "Clientes - Logs" (dashboard como código)
    └── dashboards/clientes-metricas.json        # Dashboard "Clientes - Métricas"
postman/
└── clientes-observabilidad.postman_collection.json
```

### Cómo levantarlo

1. Base de datos corriendo (ver [Base de datos con Docker](#base-de-datos-con-docker)).
2. Stack de observabilidad:

   ```bash
   cd observabilidad
   docker compose up -d
   curl http://localhost:3100/ready   # responde "ready" después de unos 15 s
   ```

3. App con el perfil `loki`:

   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=loki'
   ```

| Servicio | URL |
|----------|-----|
| API | <http://localhost:8080/api/clientes> |
| Swagger | <http://localhost:8080/swagger-ui.html> |
| Grafana | <http://localhost:3000> (usuario `admin`, contraseña `admin`) |
| Loki | <http://localhost:3100/ready> |
| Métricas de la app (crudas) | <http://localhost:8080/actuator/prometheus> |
| Prometheus | <http://localhost:9090> (en *Status → Targets*, `clientes` tiene que estar `UP`) |

Para apagar el stack: `docker compose down`. Si además agregás `-v`, se borran los logs y las métricas guardados.

### Demo paso a paso

1. **Importar la colección de Postman** `postman/clientes-observabilidad.postman_collection.json`. Cada request guarda el `X-Trace-Id` de la respuesta en la variable `{{ultimoTraceId}}` y lo imprime en la consola de Postman (*View → Show Postman Console*).
2. **Ejecutar las requests en orden** (o con *Run collection*): listar, crear, buscar, actualizar, buscar uno inexistente (404) y eliminar.
3. **Mirar la consola de la app.** Cada línea muestra `[traceId,spanId]`:

   ```
   23:13:36.101 INFO  [6af2539f8ece8232429d12e705545a6a,429d12e705545a6a] c.f.c.c.ClienteControlador - GET /api/clientes/999999
   23:13:36.102 DEBUG [6af2539f8ece8232429d12e705545a6a,429d12e705545a6a] c.f.c.s.ClienteServicioImpl - Buscando cliente clienteId=999999
   23:13:36.120 WARN  [6af2539f8ece8232429d12e705545a6a,429d12e705545a6a] c.f.c.s.ClienteServicioImpl - Cliente no encontrado clienteId=999999
   ```

4. **Mirar el archivo `logs/clientes.json`.** Tiene los mismos eventos, pero en JSON y con campos propios:

   ```json
   {"@timestamp":"...","message":"Cliente no encontrado clienteId=999999","logger_name":"com.fube.clientes.servicios.ClienteServicioImpl","level":"WARN","traceId":"6af2539f8ece8232429d12e705545a6a","spanId":"429d12e705545a6a","clienteId":999999,"app":"clientes"}
   ```

5. **Buscar en Grafana.** En *Explore*, elegí el datasource **Loki** y pegá el `traceId` del 404:

   ```
   {app="clientes"} | json | traceId="<traceId copiado de Postman>"
   ```

   Aparecen todas las líneas de esa request, y solo esas.
6. **Abrir el dashboard** desde *Dashboards → Clientes → Clientes - Logs*. Muestra los logs por nivel, un contador de clientes "no encontrados", los warnings y errores, y un buscador por `traceId` (el campo de texto de arriba).
7. **Ver las métricas crudas** en <http://localhost:8080/actuator/prometheus>. Buscá (Ctrl+F) `clientes_`: son simples contadores de texto que Prometheus lee cada 5 segundos.

   ```
   clientes_creados_total{application="clientes"} 1.0
   clientes_encontrados_total{application="clientes",operacion="listar"} 12.0
   clientes_no_encontrados_total{application="clientes",operacion="buscar"} 2.0
   ```

8. **Abrir el dashboard de métricas** desde *Dashboards → Clientes → Clientes - Métricas*. Ejecutá la colección varias veces (*Run collection* con 10 iteraciones) y mirá cómo crecen los contadores, las requests por segundo y los "no encontrados" por operación.

### Consultas LogQL útiles

| Objetivo | Consulta |
|----------|----------|
| Todos los logs de la app | `{app="clientes"}` |
| Solo warnings y errores | `{app="clientes", level=~"WARN\|ERROR"}` |
| Buscar un texto | `{app="clientes"} \|= "no encontrado"` |
| Seguir una request completa | `{app="clientes"} \| json \| traceId="<id>"` |
| Todo lo que pasó con un cliente | `{app="clientes"} \| json \| clienteId="100"` |
| Logs por minuto según nivel | `sum by (level) (count_over_time({app="clientes"}[1m]))` |

### Consultas PromQL útiles

En *Explore*, con el datasource **Prometheus**:

| Objetivo | Consulta |
|----------|----------|
| Valor actual del contador | `clientes_creados_total` |
| Clientes creados en la última hora | `increase(clientes_creados_total[1h])` |
| Clientes encontrados por operación, por minuto | `sum by (operacion) (increase(clientes_encontrados_total[1m]))` |
| "No encontrados" por operación, por minuto | `sum by (operacion) (increase(clientes_no_encontrados_total[1m]))` |
| Requests por segundo según status | `sum by (status) (rate(http_server_requests_seconds_count{uri=~"/api.*"}[1m]))` |
| Latencia p95 | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri=~"/api.*"}[5m])))` |

Un contador (`_total`) solo sube; lo interesante es cuánto subió en un período, por eso casi siempre se usa con `rate` o `increase`.

### Problemas típicos

- **No aparece nada en Loki:** revisá que el perfil `loki` esté activo (al arrancar, la app imprime `The following 1 profile is active: "loki"`) y que `curl localhost:3100/ready` responda `ready`. Los logs se mandan en lotes cada 1 segundo como máximo.
- **Grafana dice "connection refused" contra Loki:** dentro de Docker la URL es `http://loki:3100`, no `localhost`.
- **En Prometheus el target `clientes` aparece `DOWN`:** con *404*, la app que corre no tiene el endpoint (reiniciala con el código nuevo); con *connection refused*, la app no está corriendo en el puerto 8080.
- **El puerto 3000, 3100 o 9090 está ocupado:** cambiá el puerto de la izquierda en `observabilidad/docker-compose.yml` (por ejemplo `"3001:3000"`).
- **Quiero ver el SQL de Hibernate:** en `logback-spring.xml`, poné el logger `org.hibernate.SQL` en `DEBUG`. No uses `spring.jpa.show-sql`, porque escribe directo a la consola y saltea Logback.

### Para seguir

- **Trazas completas:** exportar los spans a Grafana Tempo y enlazar el `traceId` de Loki con la traza.
- **Alertas:** una regla de Grafana que avise si hay más de N errores por minuto.
