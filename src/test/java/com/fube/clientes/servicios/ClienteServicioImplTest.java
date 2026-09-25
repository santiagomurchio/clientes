package com.fube.clientes.servicios;

import com.fube.clientes.dto.ClienteDTO;
import com.fube.clientes.exception.RecursoNoEncontradoException;
import com.fube.clientes.modelos.Cliente;
import com.fube.clientes.repositorio.ClienteRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServicioImplTest {

    @Mock
    private ClienteRepositorio repositorio;

    @InjectMocks
    private ClienteServicioImpl clienteServicio;

    @Test
    void creaUnClienteYLoMapeaADTO() {
        Cliente cliente = Cliente.builder()
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.perez@test.com")
                .build();

        Cliente guardado = Cliente.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.perez@test.com")
                .build();

        when(repositorio.save(cliente)).thenReturn(guardado);

        ClienteDTO resultado = clienteServicio.crear(cliente);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("juan.perez@test.com");
    }

    @Test
    void buscaPorIdCuandoExiste() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Ana")
                .apellido("Gomez")
                .email("ana.gomez@test.com")
                .build();

        when(repositorio.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteDTO resultado = clienteServicio.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getEmail()).isEqualTo("ana.gomez@test.com");
    }

    @Test
    void lanzaExcepcionCuandoNoEncuentraElId() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteServicio.buscarPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void buscaTodosPaginado() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Luis")
                .apellido("Diaz")
                .email("luis.diaz@test.com")
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(repositorio.findAll(pageable)).thenReturn(new PageImpl<>(java.util.List.of(cliente)));

        var resultado = clienteServicio.buscarTodos(pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent())
                .extracting(ClienteDTO::getEmail)
                .containsExactly("luis.diaz@test.com");
    }

    @Test
    void actualizaUnCliente() {
        Cliente cliente = Cliente.builder()
                .id(1L)
                .nombre("Marta")
                .apellido("Lopez")
                .email("marta.lopez@test.com")
                .telefono("11-2222-2222")
                .build();

        when(repositorio.save(cliente)).thenReturn(cliente);

        ClienteDTO resultado = clienteServicio.actualizar(cliente);

        assertThat(resultado.getTelefono()).isEqualTo("11-2222-2222");
    }

    @Test
    void eliminaUnCliente() {
        clienteServicio.eliminar(1L);

        verify(repositorio, times(1)).deleteById(1L);
    }
}
