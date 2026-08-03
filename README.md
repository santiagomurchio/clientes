# Clientes

CRUD de clientes desarrollado con Spring Boot.

## Tecnologías

- Java 17
- Spring Boot 3.3
- Spring Data JPA
- Lombok
- H2 (base de datos en memoria)
- Springdoc OpenAPI (Swagger)

## Estructura del proyecto

```
com.fube.clientes
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
  -d postgres
```

| Parámetro | Valor |
|-----------|-------|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `clientesdb` |
| Usuario | `postgres` |
| Contraseña | `postgres` |
