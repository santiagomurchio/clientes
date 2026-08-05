package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClienteItemWriter implements ItemWriter<Cliente> {

    private final ClienteRepositorio repositorio;

    public ClienteItemWriter(ClienteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void write(Chunk<? extends Cliente> chunk) {
        log.info("Escribiendo chunk de {} cliente(s)", chunk.size());
        chunk.getItems().forEach(c ->
                log.info("  -> Guardando cliente id={} | telefono='{}' | direccion='{}'",
                        c.getId(), c.getTelefono(), c.getDireccion())
        );
        repositorio.saveAll(chunk.getItems());
        log.info("Chunk guardado exitosamente");
    }
}
