package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClienteItemReader extends JpaPagingItemReader<Cliente> {

    public ClienteItemReader(EntityManagerFactory entityManagerFactory) {
        setName("clienteItemReader");
        setEntityManagerFactory(entityManagerFactory);
        setQueryString("SELECT c FROM Cliente c WHERE c.telefono IS NULL OR c.direccion IS NULL");
        setPageSize(10);
    }

    @Override
    public Cliente read() throws Exception {
        Cliente cliente = super.read();
        if (cliente != null) {
            log.info("Leyendo cliente id={} | telefono={} | direccion={}",
                    cliente.getId(), cliente.getTelefono(), cliente.getDireccion());
        } else {
            log.info("Reader finalizado, no hay más clientes para procesar");
        }
        return cliente;
    }
}
