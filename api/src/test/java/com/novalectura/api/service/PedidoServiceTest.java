package com.novalectura.api.service;

import com.novalectura.api.dto.PedidoResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.*;
import com.novalectura.api.repository.CarritoRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.PedidoRepository;
import com.novalectura.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final String CORREO = "ana@mail.com";

    @Mock PedidoRepository pedidoRepository;
    @Mock CarritoRepository carritoRepository;
    @Mock LibroRepository libroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ApplicationEventPublisher eventos;
    @InjectMocks PedidoService pedidoService;

    private final Usuario ana = Usuario.builder().id(1L).nombres("Ana").apellidos("Paz")
            .correo(CORREO).rol(Rol.CLIENTE).build();
    private Carrito carrito;

    @BeforeEach
    void preparar() {
        carrito = Carrito.builder().id(7L).usuario(ana).build();
        lenient().when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(ana));
    }

    private Libro libro(Long id, String precio) {
        return Libro.builder().id(id).titulo("Libro " + id).precio(new BigDecimal(precio)).stock(10).build();
    }

    private Pedido pedidoDe(Usuario usuario) {
        Pedido pedido = Pedido.builder().id(100L).usuario(usuario).estado(EstadoPedido.REGISTRADO).build();
        pedido.agregarDetalle(DetallePedido.builder().libro(libro(10L, "20.00")).cantidad(1)
                .precioUnitario(new BigDecimal("20.00")).build());
        return pedido;
    }

    @Test
    void registrarPedido_descuentaStock_guardaElPedidoYVaciaElCarrito() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(11L, "15.50")).cantidad(1).build());
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00")).cantidad(2).build());
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(libroRepository.descontarStock(10L, 2)).thenReturn(1);
        when(libroRepository.descontarStock(11L, 1)).thenReturn(1);

        PedidoResponse respuesta = pedidoService.registrarPedido(CORREO);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).save(captor.capture());
        Pedido guardado = captor.getValue();
        assertThat(guardado.getEstado()).isEqualTo(EstadoPedido.REGISTRADO);
        assertThat(guardado.getUsuario()).isSameAs(ana);
        assertThat(guardado.getDetalles()).hasSize(2);
        assertThat(guardado.getTotal()).isEqualByComparingTo("55.50");
        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(respuesta.total()).isEqualByComparingTo("55.50");
        assertThat(respuesta.detalles()).hasSize(2);
        verify(libroRepository).descontarStock(10L, 2);
        verify(libroRepository).descontarStock(11L, 1);
    }

    @Test
    void registrarPedido_avisaQueCambiaronElCatalogoYLosPedidos() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00")).cantidad(1).build());
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(libroRepository.descontarStock(10L, 1)).thenReturn(1);

        pedidoService.registrarPedido(CORREO);

        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.PEDIDOS));
    }

    @Test
    void registrarPedido_sinStock_noAvisaDeCambios() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00")).cantidad(5).build());
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(libroRepository.descontarStock(10L, 5)).thenReturn(0);

        assertThatThrownBy(() -> pedidoService.registrarPedido(CORREO)).isInstanceOf(ConflictoException.class);

        verifyNoInteractions(eventos);
    }

    @Test
    void registrarPedido_conPrecioDelMomentoEnElDetalle() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00")).cantidad(3).build());
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(libroRepository.descontarStock(10L, 3)).thenReturn(1);

        PedidoResponse respuesta = pedidoService.registrarPedido(CORREO);

        assertThat(respuesta.detalles().getFirst().precioUnitario()).isEqualByComparingTo("20.00");
        assertThat(respuesta.detalles().getFirst().subtotal()).isEqualByComparingTo("60.00");
    }

    @Test
    void registrarPedido_carritoVacio_lanzaSolicitudInvalida() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));

        assertThatThrownBy(() -> pedidoService.registrarPedido(CORREO))
                .isInstanceOf(SolicitudInvalidaException.class);

        verify(pedidoRepository, never()).save(any());
        verifyNoInteractions(libroRepository);
    }

    @Test
    void registrarPedido_sinCarrito_lanzaSolicitudInvalida() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.registrarPedido(CORREO))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void registrarPedido_stockInsuficiente_lanzaConflictoYNoGuardaNada() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00")).cantidad(5).build());
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
        when(libroRepository.descontarStock(10L, 5)).thenReturn(0);

        assertThatThrownBy(() -> pedidoService.registrarPedido(CORREO))
                .isInstanceOf(ConflictoException.class);

        verify(pedidoRepository, never()).save(any());
        assertThat(carrito.getDetalles()).hasSize(1);
    }

    @Test
    void listarMisPedidos_devuelveLosPedidosDelCliente() {
        when(pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(1L)).thenReturn(List.of(pedidoDe(ana)));

        List<PedidoResponse> resultado = pedidoService.listarMisPedidos(CORREO);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().id()).isEqualTo(100L);
    }

    @Test
    void listarTodos_devuelveUnaPaginaDePedidos() {
        Pageable pagina = PageRequest.of(0, 10);
        when(pedidoRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(pedidoDe(ana)), pagina, 1));

        Page<PedidoResponse> resultado = pedidoService.listarTodos(pagina);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().getFirst().correoCliente()).isEqualTo(CORREO);
    }

    @Test
    void obtenerPedido_dueno_loVe() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoDe(ana)));

        assertThat(pedidoService.obtenerPedido(100L, CORREO).id()).isEqualTo(100L);
    }

    @Test
    void obtenerPedido_otroCliente_recibeNoEncontrado() {
        Usuario otro = Usuario.builder().id(2L).correo("otro@mail.com").rol(Rol.CLIENTE).build();
        when(usuarioRepository.findByCorreo("otro@mail.com")).thenReturn(Optional.of(otro));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoDe(ana)));

        assertThatThrownBy(() -> pedidoService.obtenerPedido(100L, "otro@mail.com"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPedido_administrador_veCualquierPedido() {
        Usuario admin = Usuario.builder().id(3L).correo("admin@mail.com").rol(Rol.ADMINISTRADOR).build();
        when(usuarioRepository.findByCorreo("admin@mail.com")).thenReturn(Optional.of(admin));
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoDe(ana)));

        assertThat(pedidoService.obtenerPedido(100L, "admin@mail.com").id()).isEqualTo(100L);
    }

    @Test
    void obtenerPedido_inexistente_lanzaNoEncontrado() {
        when(pedidoRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.obtenerPedido(100L, CORREO))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
