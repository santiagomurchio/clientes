package com.fube.clientes.controladores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.exception.RecursoNoEncontradoException;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.servicios.ClienteServicio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//    Tests parametrizados. Imports necesarios al descomentar:
    import org.junit.jupiter.params.ParameterizedTest;
    import org.junit.jupiter.params.provider.Arguments;
    import org.junit.jupiter.params.provider.CsvSource;
    import org.junit.jupiter.params.provider.MethodSource;
    import org.junit.jupiter.params.provider.ValueSource;
    import java.util.stream.Stream;

@WebMvcTest(ClienteControlador.class)
class ClienteControladorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteServicio servicio;

    @Test
    void buscaPorIdCuandoExiste() throws Exception {
        ClienteDTO cliente = ClienteDTO.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana.gomez@test.com")
                .build();

        when(servicio.buscarPorId(1L)).thenReturn(cliente);

//        mockMvc.perform(get("/clientes/1").header("X-USER-ID", "usuario-1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.nombre").value("Ana"))
//                .andExpect(jsonPath("$.apellido").value("Gomez"))
//                .andExpect(jsonPath("$.email").value("ana.gomez@test.com"));

        mockMvc.perform(get("/clientes/1").header("X-USER-ID", "usuario-1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cliente)));

        verify(servicio, times(1)).buscarPorId(1L);
        verifyNoMoreInteractions(servicio);
//        verificarFindById(1L);
//        verificarFindById(2L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
//        verificarFindById(1L);
    }


    private void verificarFindById(Long id) throws Exception {
        ClienteDTO cliente = ClienteDTO.builder()
                .id(id)
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana.gomez@test.com")
                .build();

        when(servicio.buscarPorId(id)).thenReturn(cliente);

//        mockMvc.perform(get("/clientes/1").header("X-USER-ID", "usuario-1"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(1))
//                .andExpect(jsonPath("$.nombre").value("Ana"))
//                .andExpect(jsonPath("$.apellido").value("Gomez"))
//                .andExpect(jsonPath("$.email").value("ana.gomez@test.com"));

        mockMvc.perform(get("/clientes/" + id.toString()).header("X-USER-ID", "usuario-1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(cliente)));

        verify(servicio, times(1)).buscarPorId(1L);
        verifyNoMoreInteractions(servicio);

    }

    @Test
    void buscaPorIdSinElHeaderDeUsuario() throws Exception {
        when(servicio.buscarPorId(1L)).thenReturn(ClienteDTO.builder().id(1L).build());

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(servicio, times(1)).buscarPorId(1L);
        verifyNoMoreInteractions(servicio);
    }

    @Test
    void devuelve404CuandoNoEncuentraElId() throws Exception {
        when(servicio.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException(99L));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.mensaje").value("Recurso no encontrado con id: 99"));

        verify(servicio, times(1)).buscarPorId(99L);
        verifyNoMoreInteractions(servicio);

    }

    @Test
    void devuelve400CuandoElIdNoEsNumerico() throws Exception {
        mockMvc.perform(get("/clientes/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }

    @Test
    void buscaTodosPaginado() throws Exception {
        ClienteDTO cliente = ClienteDTO.builder()
                .id(1L)
                .nombre("Luis")
                .apellido("Diaz")
                .email("luis.diaz@test.com")
                .build();

        Pageable pageable = PageRequest.of(0, 5);
        when(servicio.buscarTodos(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente), pageable, 1));

        mockMvc.perform(get("/clientes")
                        .param("page", "0")
                        .param("size", "5")
                        .param("versionCompleta", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("luis.diaz@test.com"))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(servicio, times(1)).buscarTodos(captor.capture());
        verifyNoMoreInteractions(servicio);

        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void creaUnClienteConLosDatosRecibidos() throws Exception {
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

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(servicio, times(1)).crear(captor.capture());
        verifyNoMoreInteractions(servicio);

        Cliente clienteCreado = captor.getValue();
        assertThat(clienteCreado.getNombre()).isEqualTo("Carla");
        assertThat(clienteCreado.getApellido()).isEqualTo("Ruiz");
        assertThat(clienteCreado.getEmail()).isEqualTo("carla.ruiz@test.com");
        assertThat(clienteCreado.getTelefono()).isEqualTo("11-4444-4444");
        assertThat(clienteCreado.getDireccion()).isEqualTo("San Martin 300");
    }

    @Test
    void devuelve400CuandoCreaSinBody() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(servicio);
    }



    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L})
    void buscaPorIdConDistintosIds(Long id) throws Exception {

        when(servicio.buscarPorId(id)).thenReturn(ClienteDTO.builder().id(id).build());

        mockMvc.perform(get("/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()));
    }









    @ParameterizedTest(name = "{0} {1} -> {2}")
    @CsvSource({
            "Juan, Perez, juan.perez@test.com",
            "Ana,  Gomez, ana.gomez@test.com"
    })
    void creaElClienteConLosDatosDelBody(String nombre, String apellido, String email) throws Exception {
        ClienteDTO clienteDTO = ClienteDTO.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .build();

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isNoContent());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(servicio).crear(captor.capture());

        assertThat(captor.getValue().getNombre()).isEqualTo(nombre);
        assertThat(captor.getValue().getApellido()).isEqualTo(apellido);
        assertThat(captor.getValue().getEmail()).isEqualTo(email);
    }

    @ParameterizedTest
    @MethodSource("clientesIncompletos")
    void creaClientesIncompletos(ClienteDTO clienteDTO) throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isNoContent());

        verify(servicio).crear(any(Cliente.class));
    }

    static Stream<Arguments> clientesIncompletos() {
        return Stream.of(
                Arguments.of(ClienteDTO.builder().nombre("Sofia").build()),
                Arguments.of(ClienteDTO.builder().nombre("Diego").telefono("11-1111-1111").build())
        );
    }
}
