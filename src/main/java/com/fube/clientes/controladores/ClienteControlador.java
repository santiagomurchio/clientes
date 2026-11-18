package com.fube.clientes.controladores;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.servicios.ClienteServicio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clientes")
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO crear(@RequestBody ClienteDTO clienteDTO) {
        log.info("POST /api/clientes");
        return servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }

    @GetMapping("/{id}")
    public ClienteDTO buscarPorId(@PathVariable Long id) {
        log.info("GET /api/clientes/{}", id);
        return servicio.buscarPorId(id);
    }

    @GetMapping
    public List<ClienteDTO> buscarTodos() {
        log.info("GET /api/clientes");
        return servicio.buscarTodos();
    }

    @PutMapping("/{id}")
    public ClienteDTO actualizar(@PathVariable Long id, @RequestBody ClienteDTO clienteDTO) {
        log.info("PUT /api/clientes/{}", id);
        clienteDTO.setId(id);
        return servicio.actualizar(ClienteMapper.toEntity(clienteDTO));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        log.info("DELETE /api/clientes/{}", id);
        servicio.eliminar(id);
    }
}
