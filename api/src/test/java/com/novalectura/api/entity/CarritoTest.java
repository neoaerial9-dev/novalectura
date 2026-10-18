package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CarritoTest {
    @Test
    @DisplayName("Crear de forma correcta un carrito con todos sus atributos")
    void crearCarritoPerfectamente() {
        Carrito carrito = new Carrito();
        LocalDateTime fecha = LocalDateTime.now();
        Usuario usuario = new Usuario();
        List<DetalleCarrito> detalles = new ArrayList<>();
        carrito.setIdCarrito(2L);
        carrito.setFechaCreacion(fecha);
        carrito.setTotal(250.0);
        carrito.setUsuario(usuario);
        carrito.setDetalles(detalles);
        assertEquals(2L, carrito.getIdCarrito());
        assertEquals(fecha, carrito.getFechaCreacion());
        assertEquals(250.0, carrito.getTotal());
        assertSame(usuario, carrito.getUsuario());
        assertSame(detalles, carrito.getDetalles());
    }

    @Test
    @DisplayName("Agregar detalle al carrito")
    void agregarDetalle() {
        Carrito carrito = new Carrito();
        DetalleCarrito detalle = new DetalleCarrito();
        carrito.setDetalles(new ArrayList<>());
        carrito.getDetalles().add(detalle);
        assertEquals(1, carrito.getDetalles().size());
        assertSame(detalle, carrito.getDetalles().getFirst());
    }
}
