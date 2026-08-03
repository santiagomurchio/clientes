package com.fube.clientes.mapper;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;

public class ClienteMapper {

    public static ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion()
        );
    }

    public static Cliente toEntity(ClienteDTO dto) {
        return new Cliente(
                dto.getId(),
                dto.getNombre(),
                dto.getApellido(),
                dto.getEmail(),
                dto.getTelefono(),
                dto.getDireccion()
        );
    }
}
