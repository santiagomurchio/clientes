CREATE TABLE clientes (
    id         BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(255) NOT NULL,
    apellido   VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    telefono   VARCHAR(50),
    direccion  VARCHAR(255)
);
