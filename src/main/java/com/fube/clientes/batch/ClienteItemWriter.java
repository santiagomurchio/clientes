package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ClienteItemWriter implements ItemWriter<Cliente> {

    private final ClienteRepositorio repositorio;

    public ClienteItemWriter(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void write(Chunk<? extends Cliente> chunk) {
        repositorio.saveAll(chunk.getItems());
    }
}
