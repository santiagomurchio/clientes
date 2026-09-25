package com.fube.clientes.controladores;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.exception.RecursoNoEncontradoException;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.servicios.ClienteServicio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    public ClienteDTO crear(ClienteDTO clienteDTO) {
        return servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }

    @GetMapping("/{id}")
    public ClienteDTO buscarPorId(@PathVariable Long id,
            @RequestHeader(value = "X-USER-ID", required = false) String xUserId) {
//        try {
            log.info("xUserId: {}", xUserId);
            ClienteDTO cliente = servicio.buscarPorId(id);
            return cliente;
//            return ResponseEntity.ok()
//                    .header("X-TIMESTAMP", String.valueOf(Instant.now().toEpochMilli()))
//                    .body(cliente);
//        } catch (RecursoNoEncontradoException ex) {
//            return ResponseEntity.notFound().build();
//        }
    }

    @GetMapping
    public Page<ClienteDTO> buscarTodos(Pageable pageable,
            @RequestParam(required = false) String versionCompleta) {
        log.info("buscarTodos - versionCompleta: {}", versionCompleta);
        return servicio.buscarTodos(pageable);
    }

    public ClienteDTO actualizarCliente(Long id, ClienteDTO clienteDTO) {
        clienteDTO.setId(id);
        return servicio.actualizar(ClienteMapper.toEntity(clienteDTO));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping
    public void crearCliente(@RequestBody ClienteDTO clienteDTO) {
        servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }


    public void eliminar(Long id) {
        servicio.eliminar(id);
    }
}
