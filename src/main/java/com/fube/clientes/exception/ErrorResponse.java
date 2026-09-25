package com.fube.clientes.exception;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje) {

    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(LocalDateTime.now(), status, error, mensaje);
    }
}
