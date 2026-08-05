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

## Estructura del proyecto

```
com.fube.clientes
├── batch            # Job, Step, Reader, Processor y Writer de Spring Batch
├── controladores    # Capa REST
├── dto              # Objetos de transferencia de datos
├── mapper           # Conversión entre entidad y DTO
├── modelos          # Entidades JPA
├── repositorio      # Acceso a datos (Spring Data JPA)
└── servicios        # Lógica de negocio
```

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/api/clientes` | Listar todos los clientes |
| `GET` | `/api/clientes/{id}` | Obtener cliente por ID |
| `POST` | `/api/clientes` | Crear cliente |
| `PUT` | `/api/clientes/{id}` | Actualizar cliente |
| `DELETE` | `/api/clientes/{id}` | Eliminar cliente |

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
