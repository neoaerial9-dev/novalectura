package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pedido")
class PedidoTest {
    private Usuario comprador;
    private Libro libroA;
    private Libro libroB;
    private Carrito carrito;

    @BeforeEach
    void iniciar() {
        comprador = Datos.usuario(1L);
        Usuario vendedor = Datos.usuario(2L);
        libroA = Datos.libro(10L, vendedor, "10.00", 5);
        libroB = Datos.libro(11L, vendedor, "25.50", 3);
        carrito = new Carrito();
        carrito.setUsuario(comprador);
    }

    @Test
    @DisplayName("registrarPedido copia las líneas del carrito, asigna al comprador y calcula el total")
    void registrarPedido_copiaLineas() {
        carrito.agregarLibro(libroA, 2);
        carrito.agregarLibro(libroB, 1);

        Pedido pedido = Pedido.registrarPedido(carrito);

        assertThat(pedido.getUsuario()).isSameAs(comprador);
        assertThat(pedido.getDetalles()).hasSize(2);
        assertThat(pedido.getDetalles()).allSatisfy(d -> assertThat(d.getPedido()).isSameAs(pedido));
        assertThat(pedido.getTotal()).isEqualByComparingTo("45.50");
    }

    @Test
    @DisplayName("un pedido nuevo queda PENDIENTE (el documento excluye pagos y seguimiento)")
    void estadoInicial() {
        carrito.agregarLibro(libroA, 1);
        assertThat(Pedido.registrarPedido(carrito).getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }

    @Test
    @DisplayName("el pedido toma el precio vigente del libro, no el que tenía en el carrito")
    void registrarPedido_precioVigente() {
        carrito.agregarLibro(libroA, 2);
        libroA.setPrecio(new BigDecimal("12.00"));

        Pedido pedido = Pedido.registrarPedido(carrito);

        assertThat(pedido.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("12.00");
        assertThat(pedido.getTotal()).isEqualByComparingTo("24.00");
    }

    @Test
    @DisplayName("no se puede registrar un pedido con el carrito vacío")
    void registrarPedido_carritoVacio() {
        assertThatThrownBy(() -> Pedido.registrarPedido(carrito))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vacío");
    }

    @Test
    @DisplayName("actualizarEstado cambia el estado")
    void actualizarEstado() {
        carrito.agregarLibro(libroA, 1);
        Pedido pedido = Pedido.registrarPedido(carrito);

        pedido.actualizarEstado(EstadoPedido.CONFIRMADO);

        assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
    }

    @Test
    @DisplayName("al persistir se registra la fecha del pedido")
    void prePersist_asignaFecha() {
        carrito.agregarLibro(libroA, 1);
        Pedido pedido = Pedido.registrarPedido(carrito);
        pedido.alCrear();
        assertThat(pedido.getFechaPedido()).isNotNull();
    }
}
