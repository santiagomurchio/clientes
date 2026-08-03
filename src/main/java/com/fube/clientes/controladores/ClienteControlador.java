package com.fube.clientes.controladores;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.mapper.ClienteMapper;
import com.fube.clientes.servicios.ClienteServicio;

import java.util.List;

public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    public ClienteDTO crear(ClienteDTO clienteDTO) {
        return servicio.crear(ClienteMapper.toEntity(clienteDTO));
    }

    public ClienteDTO buscarPorId(Long id) {
        return servicio.buscarPorId(id);
    }

    public List<ClienteDTO> buscarTodos() {
        return servicio.buscarTodos();
    }

    public ClienteDTO actualizar(Long id, ClienteDTO clienteDTO) {
        clienteDTO.setId(id);
        return servicio.actualizar(ClienteMapper.toEntity(clienteDTO));
    }

    public void eliminar(Long id) {
        servicio.eliminar(id);
    }
}
