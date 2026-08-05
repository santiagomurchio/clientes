package com.fube.clientes.repositorio;

import com.fube.clientes.modelos.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    List<Cliente> findByTelefonoIsNullOrDireccionIsNull();
}
