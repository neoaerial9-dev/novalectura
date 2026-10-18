package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DetallePedidoTest {
    @Test
    @DisplayName("Crear detalle de pedido de forma perfecta")
    void crearDetallePedido() {
        DetallePedido detalle = new DetallePedido();
        Pedido pedido = new Pedido();
        Libro libro = new Libro();
        detalle.setIdDetallePedido(4L);
        detalle.setCantidad(4);
        detalle.setPrecioUnitario(30.0);
        detalle.setSubtotal(120.0);
        detalle.setPedido(pedido);
        detalle.setLibro(libro);
        assertEquals(4L, detalle.getIdDetallePedido());
        assertEquals(4, detalle.getCantidad());
        assertEquals(30.0, detalle.getPrecioUnitario());
        assertEquals(120.0, detalle.getSubtotal());
        assertSame(pedido, detalle.getPedido());
        assertSame(libro, detalle.getLibro());
    }
}
