package com.novalectura.api.repository;

import com.novalectura.api.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByFechaPedidoDesc(Long usuarioId);
}
