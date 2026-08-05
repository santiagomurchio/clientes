package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ClienteInMemoryItemReader extends ListItemReader<Cliente> {

    public ClienteInMemoryItemReader(ClienteRepositorio repositorio) {
        super(cargarClientes(repositorio));
    }

    private static List<Cliente> cargarClientes(ClienteRepositorio repositorio) {
        List<Cliente> clientes = repositorio.findByTelefonoIsNullOrDireccionIsNull();
        log.info("Clientes cargados en memoria para procesar: {}", clientes.size());
        return clientes;
    }
}
