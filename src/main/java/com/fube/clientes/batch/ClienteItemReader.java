package com.fube.clientes.batch;

import com.fube.clientes.modelos.Cliente;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.stereotype.Component;

@Component
public class ClienteItemReader extends JpaPagingItemReader<Cliente> {

    public ClienteItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Cliente> reader = new JpaPagingItemReaderBuilder<Cliente>()
                .name("clienteItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Cliente c WHERE c.telefono IS NULL OR c.direccion IS NULL")
                .pageSize(10)
                .build();

        setName(reader.getName());
        setEntityManagerFactory(entityManagerFactory);
        setQueryString("SELECT c FROM Cliente c WHERE c.telefono IS NULL OR c.direccion IS NULL");
        setPageSize(10);
    }
}
