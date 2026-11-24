package com.novalectura.api.service;

import com.novalectura.api.dto.CarritoResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.*;
import com.novalectura.api.repository.CarritoRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final String CORREO = "ana@mail.com";

    @Mock CarritoRepository carritoRepository;
    @Mock LibroRepository libroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks CarritoService carritoService;

    private final Usuario usuario = Usuario.builder().id(1L).correo(CORREO).rol(Rol.CLIENTE).build();
    private Carrito carrito;

    @BeforeEach
    void preparar() {
        carrito = Carrito.builder().id(7L).usuario(usuario).build();
        lenient().when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        lenient().when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(carrito));
    }

    private final Usuario otroVendedor = Usuario.builder().id(99L).correo("vendedor@mail.com").rol(Rol.CLIENTE).build();

    private Libro libro(Long id, String precio, int stock) {
        return Libro.builder().id(id).titulo("Libro " + id).precio(new BigDecimal(precio)).stock(stock)
                .vendedor(otroVendedor).build();
    }

    @Test
    void obtenerCarrito_sinCarritoPrevio_creaUnoVacio() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        CarritoResponse respuesta = carritoService.obtenerCarrito(CORREO);

        assertThat(respuesta.items()).isEmpty();
        assertThat(respuesta.total()).isEqualByComparingTo("0");
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void obtenerCarrito_marcaComoNoDisponibleElLibroQueSeDioDeBaja() {
        Libro dadoDeBaja = libro(10L, "20.00", 5);
        dadoDeBaja.setActivo(false);
        carrito.agregarDetalle(DetalleCarrito.builder().libro(dadoDeBaja).cantidad(1).build());
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(11L, "10.00", 5)).cantidad(1).build());

        CarritoResponse respuesta = carritoService.obtenerCarrito(CORREO);

        assertThat(respuesta.items().get(0).disponible()).isFalse();
        assertThat(respuesta.items().get(1).disponible()).isTrue();
    }

    @Test
    void agregarLibro_nuevo_loAgregaYCalculaElTotal() {
        when(libroRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(libro(10L, "20.00", 5)));

        CarritoResponse respuesta = carritoService.agregarLibro(CORREO, 10L, 2);

        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().getFirst().cantidad()).isEqualTo(2);
        assertThat(respuesta.total()).isEqualByComparingTo("40.00");
        verify(carritoRepository).save(carrito);
    }

    @Test
    void agregarLibro_propio_lanzaSolicitudInvalida() {
        Libro propio = libro(10L, "20.00", 5);
        propio.setVendedor(usuario);
        when(libroRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(propio));

        assertThatThrownBy(() -> carritoService.agregarLibro(CORREO, 10L, 1))
                .isInstanceOf(SolicitudInvalidaException.class);

        assertThat(carrito.getDetalles()).isEmpty();
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarLibro_yaEnElCarrito_sumaLaCantidad() {
        Libro libro = libro(10L, "20.00", 5);
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro).cantidad(1).build());
        when(libroRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        CarritoResponse respuesta = carritoService.agregarLibro(CORREO, 10L, 2);

        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().getFirst().cantidad()).isEqualTo(3);
    }

    @Test
    void agregarLibro_superaElStock_lanzaConflicto() {
        Libro libro = libro(10L, "20.00", 2);
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro).cantidad(1).build());
        when(libroRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        assertThatThrownBy(() -> carritoService.agregarLibro(CORREO, 10L, 2))
                .isInstanceOf(ConflictoException.class);

        assertThat(carrito.getDetalles().getFirst().getCantidad()).isEqualTo(1);
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarLibro_libroInexistenteOInactivo_lanzaNoEncontrado() {
        when(libroRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carritoService.agregarLibro(CORREO, 10L, 1))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarCantidad_cambiaLaCantidadDelItem() {
        Libro libro = libro(10L, "20.00", 5);
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro).cantidad(1).build());

        CarritoResponse respuesta = carritoService.actualizarCantidad(CORREO, 10L, 4);

        assertThat(respuesta.items().getFirst().cantidad()).isEqualTo(4);
        assertThat(respuesta.total()).isEqualByComparingTo("80.00");
    }

    @Test
    void actualizarCantidad_superaElStock_lanzaConflicto() {
        Libro libro = libro(10L, "20.00", 3);
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro).cantidad(1).build());

        assertThatThrownBy(() -> carritoService.actualizarCantidad(CORREO, 10L, 4))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizarCantidad_libroFueraDelCarrito_lanzaNoEncontrado() {
        assertThatThrownBy(() -> carritoService.actualizarCantidad(CORREO, 10L, 2))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminarLibro_quitaElItem() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00", 5)).cantidad(1).build());
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(11L, "10.00", 5)).cantidad(1).build());

        CarritoResponse respuesta = carritoService.eliminarLibro(CORREO, 10L);

        assertThat(respuesta.items()).hasSize(1);
        assertThat(respuesta.items().getFirst().libroId()).isEqualTo(11L);
    }

    @Test
    void eliminarLibro_fueraDelCarrito_lanzaNoEncontrado() {
        assertThatThrownBy(() -> carritoService.eliminarLibro(CORREO, 10L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void vaciar_quitaTodosLosItems() {
        carrito.agregarDetalle(DetalleCarrito.builder().libro(libro(10L, "20.00", 5)).cantidad(1).build());

        carritoService.vaciar(CORREO);

        assertThat(carrito.getDetalles()).isEmpty();
        verify(carritoRepository).save(carrito);
    }
}
