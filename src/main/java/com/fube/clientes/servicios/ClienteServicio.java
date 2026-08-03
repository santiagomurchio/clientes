package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;

import java.util.List;

public interface ClienteServicio {

    ClienteDTO crear(Cliente cliente);
    ClienteDTO buscarPorId(Long id);
    List<ClienteDTO> buscarTodos();
    ClienteDTO actualizar(Cliente cliente);
    void eliminar(Long id);
}
