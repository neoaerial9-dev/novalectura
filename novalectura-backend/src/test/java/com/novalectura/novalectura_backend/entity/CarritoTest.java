package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Carrito")
class CarritoTest {
    private Libro libroA;
    private Libro libroB;
    private Carrito carrito;

    @BeforeEach
    void iniciar() {
        Usuario vendedor = Datos.usuario(2L);
        libroA = Datos.libro(10L, vendedor, "10.00", 5);
        libroB = Datos.libro(11L, vendedor, "25.50", 3);
        carrito = new Carrito();
        carrito.setUsuario(Datos.usuario(1L));
    }

    @Test
    @DisplayName("un carrito nuevo está vacío y con total 0")
    void carritoNuevo() {
        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("agregar un libro crea una línea con precio, subtotal y total")
    void agregarLibro_nuevo() {
        carrito.agregarLibro(libroA, 2);

        assertThat(carrito.getDetalles()).hasSize(1);
        DetalleCarrito detalle = carrito.getDetalles().get(0);
        assertThat(detalle.getCantidad()).isEqualTo(2);
        assertThat(detalle.getPrecioUnitario()).isEqualByComparingTo("10.00");
        assertThat(detalle.getSubtotal()).isEqualByComparingTo("20.00");
        assertThat(carrito.getTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("agregar el mismo libro suma la cantidad en la misma línea (no duplica filas)")
    void agregarLibro_mismoLibroSuma() {
        carrito.agregarLibro(libroA, 2);
        carrito.agregarLibro(libroA, 3);

        assertThat(carrito.getDetalles()).hasSize(1);
        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(5);
        assertThat(carrito.getTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("libros distintos generan líneas distintas y el total las suma")
    void agregarLibro_dosLibros() {
        carrito.agregarLibro(libroA, 2);
        carrito.agregarLibro(libroB, 1);

        assertThat(carrito.getDetalles()).hasSize(2);
        assertThat(carrito.getTotal()).isEqualByComparingTo("45.50");
    }

    @Test
    @DisplayName("la cantidad al agregar debe ser mayor a 0")
    void agregarLibro_cantidadInvalida() {
        assertThatThrownBy(() -> carrito.agregarLibro(libroA, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carrito.agregarLibro(libroA, -2)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> carrito.agregarLibro(libroA, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(carrito.getDetalles()).isEmpty();
    }

    @Test
    @DisplayName("eliminar una línea existente la quita y recalcula el total")
    void eliminarLibro_existente() {
        carrito.agregarLibro(libroA, 2);
        carrito.agregarLibro(libroB, 1);
        carrito.getDetalles().get(0).setIdDetalle(100L);

        boolean eliminado = carrito.eliminarLibro(100L);

        assertThat(eliminado).isTrue();
        assertThat(carrito.getDetalles()).hasSize(1);
        assertThat(carrito.getTotal()).isEqualByComparingTo("25.50");
    }

    @Test
    @DisplayName("eliminar una línea que no está devuelve false y no cambia nada")
    void eliminarLibro_inexistente() {
        carrito.agregarLibro(libroA, 2);
        carrito.getDetalles().get(0).setIdDetalle(100L);

        assertThat(carrito.eliminarLibro(999L)).isFalse();
        assertThat(carrito.getDetalles()).hasSize(1);
        assertThat(carrito.getTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("actualizar la cantidad recalcula subtotal y total")
    void actualizarCantidad_ok() {
        carrito.agregarLibro(libroA, 2);
        carrito.getDetalles().get(0).setIdDetalle(100L);

        boolean actualizado = carrito.actualizarCantidad(100L, 4);

        assertThat(actualizado).isTrue();
        assertThat(carrito.getDetalles().get(0).getSubtotal()).isEqualByComparingTo("40.00");
        assertThat(carrito.getTotal()).isEqualByComparingTo("40.00");
    }

    @Test
    @DisplayName("actualizar una línea que no existe devuelve false")
    void actualizarCantidad_inexistente() {
        carrito.agregarLibro(libroA, 2);
        assertThat(carrito.actualizarCantidad(999L, 4)).isFalse();
        assertThat(carrito.getTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("actualizar con cantidad inválida lanza error y no modifica la línea")
    void actualizarCantidad_invalida() {
        carrito.agregarLibro(libroA, 2);
        carrito.getDetalles().get(0).setIdDetalle(100L);

        assertThatThrownBy(() -> carrito.actualizarCantidad(100L, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("vaciar el carrito deja sin líneas y con total 0")
    void vaciarCarrito() {
        carrito.agregarLibro(libroA, 2);
        carrito.vaciarCarrito();

        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("el carrito conserva el precio de cuando se agregó (el servicio es quien lo sincroniza)")
    void calcularTotal_usaPrecioGuardado() {
        carrito.agregarLibro(libroA, 2);
        libroA.setPrecio(new BigDecimal("15.00"));

        assertThat(carrito.calcularTotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("al persistir se registra la fecha de creación")
    void prePersist_asignaFecha() {
        carrito.alCrear();
        assertThat(carrito.getFechaCreacion()).isNotNull();
    }
}
