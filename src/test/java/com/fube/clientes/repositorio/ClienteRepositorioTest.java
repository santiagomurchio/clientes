package com.fube.clientes.repositorio;

import com.fube.clientes.modelos.Cliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClienteRepositorioTest {

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @Test
    void guardaYRecuperaUnCliente() {
        Cliente cliente = Cliente.builder()
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.perez@test.com")
                .telefono("11-1234-5678")
                .direccion("Calle Falsa 123")
                .build();

        Cliente guardado = clienteRepositorio.save(cliente);

        Optional<Cliente> encontrado = clienteRepositorio.findById(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEmail()).isEqualTo("juan.perez@test.com");
    }

    @Test
    @Sql(scripts = "classpath:scripts/clientes-test-data.sql")
    void encuentraClientesConTelefonoODireccionNula() {
        List<Cliente> incompletos = clienteRepositorio.findByTelefonoIsNullOrDireccionIsNull();

        assertThat(incompletos)
                .extracting(Cliente::getEmail)
                .containsExactlyInAnyOrder("sofia.alvarez@test.com", "diego.torres@test.com");
    }

    @Test
    void noPermiteEmailsDuplicados() {
        Cliente cliente1 = Cliente.builder()
                .nombre("Pedro")
                .apellido("Ramirez")
                .email("duplicado@test.com")
                .build();

        Cliente cliente2 = Cliente.builder()
                .nombre("Pablo")
                .apellido("Sanchez")
                .email("duplicado@test.com")
                .build();

        clienteRepositorio.saveAndFlush(cliente1);

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> clienteRepositorio.saveAndFlush(cliente2)
        );
    }
}
