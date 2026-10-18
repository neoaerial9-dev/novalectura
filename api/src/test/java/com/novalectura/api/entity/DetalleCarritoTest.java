package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DetalleCarritoTest {
    @Test
    @DisplayName("Crear de forma correcta un detalle de carrito")
    void crearDetalleCarrito() {
        DetalleCarrito detalle = new DetalleCarrito();
        Carrito carrito = new Carrito();
        Libro libro = new Libro();
        detalle.setIdDetalleCarrito(7L);
        detalle.setCantidad(13);
        detalle.setPrecioUnitario(25.0);
        detalle.setSubtotal(75.0);
        detalle.setCarrito(carrito);
        detalle.setLibro(libro);
        assertEquals(7L, detalle.getIdDetalleCarrito());
        assertEquals(13, detalle.getCantidad());
        assertEquals(25.0, detalle.getPrecioUnitario());
        assertEquals(75.0, detalle.getSubtotal());
        assertSame(carrito, detalle.getCarrito());
        assertSame(libro, detalle.getLibro());
    }
}
