package com.novalectura.novalectura_backend.repository;

import com.novalectura.novalectura_backend.entity.Carrito;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    @EntityGraph(attributePaths = {"detalles", "detalles.libro"})
    Optional<Carrito> findByUsuario_IdUsuario(Long idUsuario);
}
