package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DetalleCarrito y DetallePedido")
class DetalleTest {
    private final Libro libro = Datos.libro(10L, Datos.usuario(2L), "12.50", 5);

    @Test
    @DisplayName("DetalleCarrito copia el precio del libro y calcula el subtotal")
    void detalleCarrito_calculaSubtotal() {
        DetalleCarrito detalle = new DetalleCarrito(new Carrito(), libro, 3);

        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("12.50");
        assertThat(detalle.getSubtotal()).isEqualByComparingTo("37.50");
        assertThat(detalle.getLibro()).isSameAs(libro);
    }

    @Test
    @DisplayName("DetalleCarrito recalcula el subtotal si cambia la cantidad")
    void detalleCarrito_recalcula() {
        DetalleCarrito detalle = new DetalleCarrito(new Carrito(), libro, 1);
        detalle.setCantidad(4);

        assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("50.00");
        assertThat(detalle.getSubtotal()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("DetallePedido guarda el precio histórico aunque el vendedor cambie el precio después")
    void detallePedido_precioHistorico() {
        DetallePedido detalle = new DetallePedido(new Pedido(), libro, 2);

        libro.setPrecio(new BigDecimal("99.00"));

        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("12.50");
        assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("25.00");
    }
}
