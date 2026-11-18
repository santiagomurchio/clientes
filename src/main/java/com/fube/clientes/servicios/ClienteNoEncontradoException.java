package com.fube.clientes.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se busca un cliente que no existe. Spring la traduce a un 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNoEncontradoException extends RuntimeException {

    public ClienteNoEncontradoException(Long id) {
        super("Cliente no encontrado con id: " + id);
    }
}
