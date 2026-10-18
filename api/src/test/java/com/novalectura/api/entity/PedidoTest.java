package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class PedidoTest {
    @Test
    @DisplayName("Crear de forma perfecta un pedido")
    void crearPedido() {
        Pedido pedido = new Pedido();
        LocalDateTime fecha = LocalDateTime.now();
        Usuario usuario = new Usuario();
        List<DetallePedido> detalles = new ArrayList<>();
        pedido.setIdPedido(5L);
        pedido.setFechaPedido(fecha);
        pedido.setTotal(350.0);
        pedido.setEstado("PENDIENTE");
        pedido.setUsuario(usuario);
        pedido.setDetalles(detalles);
        assertEquals(5L, pedido.getIdPedido());
        assertEquals(fecha, pedido.getFechaPedido());
        assertEquals(350.0, pedido.getTotal());
        assertEquals("PENDIENTE", pedido.getEstado());
        assertSame(usuario, pedido.getUsuario());
        assertSame(detalles, pedido.getDetalles());
    }

    @Test
    @DisplayName("Agregar detalle de pedido")
    void agregarDetallePedido() {
        Pedido pedido = new Pedido();
        DetallePedido detalle = new DetallePedido();
        pedido.setDetalles(new ArrayList<>());
        pedido.getDetalles().add(detalle);
        assertEquals(1, pedido.getDetalles().size());
        assertSame(detalle, pedido.getDetalles().getFirst());
    }
}
