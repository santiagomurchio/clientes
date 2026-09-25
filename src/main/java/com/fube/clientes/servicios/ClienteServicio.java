package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteServicio {

    ClienteDTO crear(Cliente cliente);
    ClienteDTO buscarPorId(Long id);
    Page<ClienteDTO> buscarTodos(Pageable pageable);
    ClienteDTO actualizar(Cliente cliente);
    void eliminar(Long id);
}
