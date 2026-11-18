package com.fube.clientes.observabilidad;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Devuelve el traceId de la request en el header de respuesta "X-Trace-Id".
 *
 * Micrometer Tracing ya genera un traceId por request y lo pone en el MDC, así que
 * aparece en todas las líneas de log de esa request. Este filtro solo lo expone al
 * cliente (Postman, frontend, otro servicio) para poder buscarlo después en Grafana.
 */
@Component
public class TraceIdResponseFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Trace-Id";

    private final Tracer tracer;

    public TraceIdResponseFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Span span = tracer.currentSpan();
        if (span != null) {
            response.setHeader(HEADER, span.context().traceId());
        }
        chain.doFilter(request, response);
    }
}
