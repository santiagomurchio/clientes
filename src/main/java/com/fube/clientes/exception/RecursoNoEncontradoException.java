package com.fube.clientes.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(Long id) {
        super("Recurso no encontrado con id: " + id);
    }
}
