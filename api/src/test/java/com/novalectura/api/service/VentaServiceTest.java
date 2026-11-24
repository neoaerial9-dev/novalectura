package com.novalectura.api.service;

import com.novalectura.api.dto.VentaResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.*;
import com.novalectura.api.repository.DetallePedidoRepository;
import com.novalectura.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock DetallePedidoRepository detallePedidoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks VentaService ventaService;

    private final Usuario vendedor = Usuario.builder().id(1L).correo("ana@mail.com").rol(Rol.CLIENTE).build();
    private final Usuario comprador = Usuario.builder().id(2L).nombres("Luis").apellidos("Gil")
            .correo("luis@mail.com").rol(Rol.CLIENTE).build();

    @Test
    void listarMisVentas_devuelveLasLineasVendidasConLosDatosDelComprador() {
        Libro libro = Libro.builder().id(10L).titulo("Dune").vendedor(vendedor).build();
        Pedido pedido = Pedido.builder().id(100L).usuario(comprador).estado(EstadoPedido.REGISTRADO).build();
        pedido.agregarDetalle(DetallePedido.builder().libro(libro).cantidad(2)
                .precioUnitario(new BigDecimal("24.99")).build());
        when(usuarioRepository.findByCorreo("ana@mail.com")).thenReturn(Optional.of(vendedor));
        when(detallePedidoRepository.buscarVentasDelVendedor(1L)).thenReturn(pedido.getDetalles());

        List<VentaResponse> ventas = ventaService.listarMisVentas("ana@mail.com");

        assertThat(ventas).hasSize(1);
        VentaResponse venta = ventas.getFirst();
        assertThat(venta.pedidoId()).isEqualTo(100L);
        assertThat(venta.titulo()).isEqualTo("Dune");
        assertThat(venta.cantidad()).isEqualTo(2);
        assertThat(venta.subtotal()).isEqualByComparingTo("49.98");
        assertThat(venta.comprador()).isEqualTo("Luis Gil");
        assertThat(venta.correoComprador()).isEqualTo("luis@mail.com");
    }

    @Test
    void listarMisVentas_usuarioInexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findByCorreo("nadie@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ventaService.listarMisVentas("nadie@mail.com"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
