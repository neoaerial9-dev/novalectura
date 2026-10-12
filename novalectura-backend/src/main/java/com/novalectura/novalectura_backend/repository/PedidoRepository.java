package com.novalectura.novalectura_backend.repository;

import com.novalectura.novalectura_backend.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Page<Pedido> findByUsuario_IdUsuario(Long idUsuario, Pageable pageable);

    @EntityGraph(attributePaths = {"detalles", "detalles.libro"})
    Optional<Pedido> findWithDetallesByIdPedido(Long idPedido);
}
