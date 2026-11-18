package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Los logs usan kv(...) de logstash-logback-encoder: en consola se imprime "clienteId=5"
 * y en el JSON queda como un campo propio ("clienteId": 5) que se puede filtrar en Loki.
 * Nunca logueamos datos sensibles del cliente (email, teléfono, dirección).
 *
 * Métricas de negocio (Micrometer): se exponen en /actuator/prometheus como
 *   clientes_creados_total
 *   clientes_encontrados_total{operacion="buscar|listar"}
 *   clientes_no_encontrados_total{operacion="buscar|actualizar|eliminar"}
 */
@Slf4j
@Service
public class ClienteServicioImpl implements ClienteServicio {

    private final ClienteRepositorio repositorio;
    private final MeterRegistry meterRegistry;
    private final Counter clientesCreados;

    public ClienteServicioImpl(ClienteRepositorio repositorio, MeterRegistry meterRegistry) {
        this.repositorio = repositorio;
        this.meterRegistry = meterRegistry;
        this.clientesCreados = Counter.builder("clientes.creados")
                .description("Cantidad de clientes creados")
                .register(meterRegistry);
    }

    /** Un contador por operación: el tag "operacion" permite separarlos en Grafana. */
    private void contarNoEncontrado(String operacion) {
        meterRegistry.counter("clientes.no.encontrados", "operacion", operacion).increment();
    }

    /** Suma "cantidad" clientes encontrados: 1 al buscar por id, N al listar. */
    private void contarEncontrados(String operacion, int cantidad) {
        meterRegistry.counter("clientes.encontrados", "operacion", operacion).increment(cantidad);
    }

    @Override
    public ClienteDTO crear(Cliente cliente) {
        ClienteDTO creado = ClienteMapper.toDTO(repositorio.save(cliente));
        clientesCreados.increment();
        log.info("Cliente creado {}", kv("clienteId", creado.getId()));
        return creado;
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        log.debug("Buscando cliente {}", kv("clienteId", id));
        return repositorio.findById(id)
                .map(cliente -> {
                    contarEncontrados("buscar", 1);
                    return ClienteMapper.toDTO(cliente);
                })
                .orElseThrow(() -> {
                    contarNoEncontrado("buscar");
                    log.warn("Cliente no encontrado {}", kv("clienteId", id));
                    return new ClienteNoEncontradoException(id);
                });
    }

    @Override
    public List<ClienteDTO> buscarTodos() {
        List<ClienteDTO> clientes = repositorio.findAll().stream()
                .map(ClienteMapper::toDTO)
                .toList();
        contarEncontrados("listar", clientes.size());
        log.info("Listado de clientes {}", kv("cantidad", clientes.size()));
        return clientes;
    }

    @Override
    public ClienteDTO actualizar(Cliente cliente) {
        if (!repositorio.existsById(cliente.getId())) {
            contarNoEncontrado("actualizar");
            log.warn("Intento de actualizar cliente inexistente {}", kv("clienteId", cliente.getId()));
            throw new ClienteNoEncontradoException(cliente.getId());
        }
        ClienteDTO actualizado = ClienteMapper.toDTO(repositorio.save(cliente));
        log.info("Cliente actualizado {}", kv("clienteId", actualizado.getId()));
        return actualizado;
    }

    @Override
    public void eliminar(Long id) {
        if (!repositorio.existsById(id)) {
            contarNoEncontrado("eliminar");
            log.warn("Intento de eliminar cliente inexistente {}", kv("clienteId", id));
            throw new ClienteNoEncontradoException(id);
        }
        repositorio.deleteById(id);
        log.info("Cliente eliminado {}", kv("clienteId", id));
    }
}
