package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class ClienteItemProcessor implements ItemProcessor<Cliente, Cliente> {

    static final String TELEFONO_DEFAULT = "0000-0000";
    static final String DIRECCION_DEFAULT = "Sin domicilio";

    @Override
    public Cliente process(Cliente cliente) {
        if (cliente.getTelefono() == null) {
            cliente.setTelefono(TELEFONO_DEFAULT);
        }
        if (cliente.getDireccion() == null) {
            cliente.setDireccion(DIRECCION_DEFAULT);
        }
        return cliente;
    }
}
