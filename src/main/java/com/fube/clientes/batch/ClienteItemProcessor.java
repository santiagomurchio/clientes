package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClienteItemProcessor implements ItemProcessor<Cliente, Cliente> {

    static final String TELEFONO_DEFAULT = "0000-0000";
    static final String DIRECCION_DEFAULT = "Sin domicilio";

    @Override
    public Cliente process(Cliente cliente) {
        log.info("Procesando cliente id={}", cliente.getId());

        if (cliente.getTelefono() == null) {
            cliente.setTelefono(TELEFONO_DEFAULT);
            log.info("  -> telefono nulo, asignando valor por defecto: '{}'", TELEFONO_DEFAULT);
        }
        if (cliente.getDireccion() == null) {
            cliente.setDireccion(DIRECCION_DEFAULT);
            log.info("  -> direccion nula, asignando valor por defecto: '{}'", DIRECCION_DEFAULT);
        }

        return cliente;
    }
}
