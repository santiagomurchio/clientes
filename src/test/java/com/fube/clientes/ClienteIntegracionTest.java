package com.fube.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepositorio clienteRepositorio;

    @AfterEach
    void limpiarBaseDeDatos() {
        clienteRepositorio.deleteAll();
    }

    @Test
    void creaUnClienteYLoGuardaEnLaBaseDeDatos() throws Exception {
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .nombre("Carla")
                .apellido("Ruiz")
                .email("carla.ruiz@test.com")
                .telefono("11-4444-4444")
                .direccion("San Martin 300")
                .build();

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isNoContent());

        List<Cliente> clientes = clienteRepositorio.findAll();

        assertThat(clientes).hasSize(1);
        assertThat(clientes.get(0).getId()).isNotNull();
        assertThat(clientes.get(0).getNombre()).isEqualTo("Carla");
        assertThat(clientes.get(0).getApellido()).isEqualTo("Ruiz");
        assertThat(clientes.get(0).getEmail()).isEqualTo("carla.ruiz@test.com");
        assertThat(clientes.get(0).getTelefono()).isEqualTo("11-4444-4444");
        assertThat(clientes.get(0).getDireccion()).isEqualTo("San Martin 300");
    }

    @Test
    void buscaPorIdUnClienteGuardadoEnLaBaseDeDatos() throws Exception {
        Cliente guardado = clienteRepositorio.save(Cliente.builder()
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana.gomez@test.com")
                .build());

        mockMvc.perform(get("/clientes/" + guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId().intValue()))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.apellido").value("Gomez"))
                .andExpect(jsonPath("$.email").value("ana.gomez@test.com"));
    }

    @Test
    void devuelve404CuandoElClienteNoExisteEnLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/clientes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Recurso no encontrado con id: 999"));
    }

    @Test
    @Sql(scripts = "classpath:scripts/clientes-test-data.sql")
    void buscaTodosPaginadoDesdeLaBaseDeDatos() throws Exception {
        mockMvc.perform(get("/clientes")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "apellido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].email").value("sofia.alvarez@test.com"))
                .andExpect(jsonPath("$.content[1].email").value("carlos.fernandez@test.com"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}
