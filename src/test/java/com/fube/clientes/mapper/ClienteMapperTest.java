package com.fube.clientes.mapper;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    @BeforeAll
    public static void beforeAll() {
        System.out.println("BeforeAll()");
    }

    @BeforeEach
    public void setup() {
        System.out.println("BeforeEach()");
    }

    @AfterEach
    public void tearDown() {
        System.out.println("AfterEach()");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("AfterAll()");
    }

    @Test
    void mapeaClienteADTO() {
//        System.out.println("BeforeEach()");

        System.out.println("mapeaClienteADTO()");

        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.perez@test.com")
                .telefono("11-1234-5678")
                .direccion("Calle Falsa 123")
                .build();

        ClienteDTO dto = ClienteMapper.toDTO(cliente);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNombre()).isEqualTo("Juan");
        assertThat(dto.getApellido()).isEqualTo("Perez");
        assertThat(dto.getEmail()).isEqualTo("juan.perez@test.com");
        assertThat(dto.getTelefono()).isEqualTo("11-1234-5678");
        assertThat(dto.getDireccion()).isEqualTo("Calle Falsa 123");
    }

    @Test
    void mapeaDTOACliente() {
//        System.out.println("BeforeEach()");

        System.out.println("mapeaDTOACliente()");

        ClienteDTO dto = ClienteDTO.builder()
                .id(2L)
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana.gomez@test.com")
                .telefono("11-8765-4321")
                .direccion("Avenida Siempre Viva 742")
                .build();

        Cliente cliente = ClienteMapper.toEntity(dto);

        assertThat(cliente.getId()).isEqualTo(2L);
        assertThat(cliente.getNombre()).isEqualTo("Ana");
        assertThat(cliente.getApellido()).isEqualTo("Gomez");
        assertThat(cliente.getEmail()).isEqualTo("ana.gomez@test.com");
        assertThat(cliente.getTelefono()).isEqualTo("11-8765-4321");
        assertThat(cliente.getDireccion()).isEqualTo("Avenida Siempre Viva 742");
    }
}
