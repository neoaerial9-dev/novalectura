package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.PedidoResponse;
import com.novalectura.novalectura_backend.entity.Carrito;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Pedido;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.mapper.PedidoMapper;
import com.novalectura.novalectura_backend.repository.CarritoRepository;
import com.novalectura.novalectura_backend.repository.PedidoRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService")
class PedidoServiceTest {
    @Mock PedidoRepository pedidos;
    @Mock CarritoRepository carritos;
    @Mock PedidoMapper mapper;
    @InjectMocks PedidoService servicio;

    private Usuario comprador;
    private UsuarioDetails actual;
    private Libro libroA;
    private Libro libroB;
    private Carrito carrito;
    private final PedidoResponse respuesta =
            new PedidoResponse(1L, LocalDateTime.now(), BigDecimal.ZERO, EstadoPedido.PENDIENTE, null, List.of());

    @BeforeEach
    void iniciar() {
        comprador = Datos.usuario(5L);
        actual = new UsuarioDetails(comprador);
        Usuario vendedor = Datos.usuario(9L);
        libroA = Datos.libro(10L, vendedor, "10.00", 5);
        libroB = Datos.libro(11L, vendedor, "20.50", 1);
        carrito = new Carrito();
        carrito.setUsuario(comprador);
    }

    private static Pedido pedidoDe(Long id, Usuario usuario) {
        Pedido pedido = new Pedido();
        pedido.setIdPedido(id);
        pedido.setUsuario(usuario);
        return pedido;
    }

    @Test
    @DisplayName("sin carrito responde 400")
    void registrar_sinCarrito() {
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.BAD_REQUEST, () -> servicio.registrar(actual));

        verify(pedidos, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("con el carrito vacío responde 400")
    void registrar_carritoVacio() {
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.BAD_REQUEST, () -> servicio.registrar(actual));

        verify(pedidos, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("registra el pedido, descuenta el stock de cada libro y vacía el carrito")
    void registrar_ok() {
        carrito.agregarLibro(libroA, 2);
        carrito.agregarLibro(libroB, 1);
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));
        when(mapper.toResponse(any(Pedido.class))).thenReturn(respuesta);

        PedidoResponse resultado = servicio.registrar(actual);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidos).save(captor.capture());
        Pedido guardado = captor.getValue();
        assertThat(guardado.getUsuario()).isSameAs(comprador);
        assertThat(guardado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(guardado.getDetalles()).hasSize(2);
        assertThat(guardado.getTotal()).isEqualByComparingTo("40.50");

        assertThat(libroA.getStock()).as("stock A: 5 - 2").isEqualTo(3);
        assertThat(libroB.getStock()).as("stock B: 1 - 1").isZero();

        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo("0");
        assertThat(resultado).isSameAs(respuesta);
    }

    @Test
    @DisplayName("un libro dado de baja tras agregarlo: 409, no se crea el pedido ni se toca el stock")
    void registrar_libroDadoDeBaja() {
        carrito.agregarLibro(libroA, 2);
        libroA.setActivo(false);
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.registrar(actual));

        assertThat(libroA.getStock()).isEqualTo(5);
        assertThat(carrito.getDetalles()).as("el carrito se conserva").hasSize(1);
        verify(pedidos, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("stock insuficiente al confirmar: 409, no se crea el pedido ni se toca el stock")
    void registrar_stockInsuficiente() {
        carrito.agregarLibro(libroA, 4);
        libroA.setStock(3);     // otro comprador se llevó ejemplares mientras tanto
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.registrar(actual));

        assertThat(libroA.getStock()).isEqualTo(3);
        verify(pedidos, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("todo o nada: si el segundo libro no alcanza, el stock del primero tampoco se descuenta")
    void registrar_validaTodoAntesDeDescontar() {
        carrito.agregarLibro(libroA, 2);      // este sí alcanza
        carrito.agregarLibro(libroB, 1);
        libroB.setStock(0);                   // este ya no
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.registrar(actual));

        assertThat(libroA.getStock()).as("no se descontó nada").isEqualTo(5);
        verify(pedidos, never()).save(any(Pedido.class));
    }

    @Test
    @DisplayName("un usuario normal solo ve SU historial (y nunca llama a findAll)")
    void listar_usuario() {
        Page<Pedido> pagina = new PageImpl<>(List.of());
        when(pedidos.findByUsuario_IdUsuario(eq(5L), any(Pageable.class))).thenReturn(pagina);

        PageResponse<PedidoResponse> resultado = servicio.listar(actual, 0, 12);

        assertThat(resultado.contenido()).isEmpty();
        verify(pedidos, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("el ADMIN ve los pedidos de todos")
    void listar_admin() {
        UsuarioDetails admin = new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN));
        Page<Pedido> pagina = new PageImpl<>(List.of());
        when(pedidos.findAll(any(Pageable.class))).thenReturn(pagina);

        servicio.listar(admin, 0, 12);

        verify(pedidos).findAll(any(Pageable.class));
        verify(pedidos, never()).findByUsuario_IdUsuario(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("los pedidos se listan del más reciente al más antiguo y el tamaño se acota a 50")
    void listar_ordenYTamano() {
        when(pedidos.findByUsuario_IdUsuario(eq(5L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        servicio.listar(actual, -1, 500);

        verify(pedidos).findByUsuario_IdUsuario(eq(5L), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
        Sort.Order orden = captor.getValue().getSort().getOrderFor("fechaPedido");
        assertThat(orden).isNotNull();
        assertThat(orden.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("el comprador puede ver su propio pedido")
    void obtener_propio() {
        Pedido pedido = pedidoDe(1L, comprador);
        when(pedidos.findWithDetallesByIdPedido(1L)).thenReturn(Optional.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(respuesta);

        assertThat(servicio.obtener(actual, 1L)).isSameAs(respuesta);
    }

    @Test
    @DisplayName("el pedido de otro usuario responde 404 (no 403) para no revelar que existe")
    void obtener_ajeno() {
        when(pedidos.findWithDetallesByIdPedido(1L)).thenReturn(Optional.of(pedidoDe(1L, Datos.usuario(77L))));

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.obtener(actual, 1L));
    }

    @Test
    @DisplayName("el ADMIN puede ver el pedido de cualquiera")
    void obtener_admin() {
        UsuarioDetails admin = new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN));
        Pedido pedido = pedidoDe(1L, Datos.usuario(77L));
        when(pedidos.findWithDetallesByIdPedido(1L)).thenReturn(Optional.of(pedido));
        when(mapper.toResponse(pedido)).thenReturn(respuesta);

        assertThat(servicio.obtener(admin, 1L)).isSameAs(respuesta);
    }

    @Test
    @DisplayName("un pedido inexistente responde 404")
    void obtener_noExiste() {
        when(pedidos.findWithDetallesByIdPedido(1L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.obtener(actual, 1L));
    }
}
