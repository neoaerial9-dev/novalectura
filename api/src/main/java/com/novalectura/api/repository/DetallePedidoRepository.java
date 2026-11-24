package com.novalectura.api.repository;

import com.novalectura.api.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
            select d from DetallePedido d
            where d.libro.vendedor.id = :vendedorId
            order by d.pedido.fechaPedido desc, d.id desc
            """)
    List<DetallePedido> buscarVentasDelVendedor(@Param("vendedorId") Long vendedorId);
}
