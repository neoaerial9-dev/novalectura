package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CarritoCantidadRequest;
import com.novalectura.novalectura_backend.dto.request.CarritoItemRequest;
import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.entity.Carrito;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.mapper.CarritoMapper;
import com.novalectura.novalectura_backend.repository.CarritoRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoService")
class CarritoServiceTest {
    @Mock CarritoRepository carritos;
    @Mock LibroRepository libros;
    @Mock UsuarioRepository usuarios;
    @Mock CarritoMapper mapper;
    @InjectMocks CarritoService servicio;

    private Usuario comprador;
    private UsuarioDetails actual;
    private Libro libro;
    private Carrito carrito;
    private final CarritoResponse respuesta = new CarritoResponse(1L, BigDecimal.ZERO, List.of());

    @BeforeEach
    void iniciar() {
        comprador = Datos.usuario(5L);
        actual = new UsuarioDetails(comprador);
        libro = Datos.libro(10L, Datos.usuario(9L), "10.00", 3);
        carrito = new Carrito();
        carrito.setUsuario(comprador);
    }

    private void conCarritoExistente() {
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));
        when(mapper.toResponse(carrito)).thenReturn(respuesta);
    }

    @Test
    @DisplayName("agregar un libro lo pone en el carrito con su precio y total, y persiste los cambios")
    void agregar_ok() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        conCarritoExistente();

        CarritoResponse resultado = servicio.agregar(actual, new CarritoItemRequest(10L, 2));

        assertThat(resultado).isSameAs(respuesta);
        assertThat(carrito.getDetalles()).hasSize(1);
        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(2);
        assertThat(carrito.getTotal()).isEqualByComparingTo("20.00");
        verify(carritos).saveAndFlush(carrito);
    }

    @Test
    @DisplayName("si el usuario aún no tiene carrito, se crea uno a su nombre")
    void agregar_creaCarrito() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.empty());
        when(usuarios.getReferenceById(5L)).thenReturn(comprador);
        when(carritos.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Carrito.class))).thenReturn(respuesta);

        servicio.agregar(actual, new CarritoItemRequest(10L, 1));

        verify(carritos).save(any(Carrito.class));
    }

    @Test
    @DisplayName("NO se puede agregar al carrito el propio libro: 400")
    void agregar_libroPropio() {
        Libro propio = Datos.libro(10L, comprador, "10.00", 3);
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(propio));

        assertApi(HttpStatus.BAD_REQUEST, () -> servicio.agregar(actual, new CarritoItemRequest(10L, 1)));

        verifyNoInteractions(carritos);
    }

    @Test
    @DisplayName("un libro inexistente o dado de baja responde 404")
    void agregar_libroNoExiste() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.agregar(actual, new CarritoItemRequest(10L, 1)));
    }

    @Test
    @DisplayName("no se puede pedir más que el stock: 409")
    void agregar_stockInsuficiente() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.agregar(actual, new CarritoItemRequest(10L, 4)));

        assertThat(carrito.getDetalles()).isEmpty();
    }

    @Test
    @DisplayName("el stock se valida contra lo que YA hay en el carrito (2 + 2 > 3): 409 y la línea queda igual")
    void agregar_stockAcumulado() {
        carrito.agregarLibro(libro, 2);
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.agregar(actual, new CarritoItemRequest(10L, 2)));

        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("al consultar el carrito se retiran los libros que el vendedor dio de baja")
    void obtener_retiraLibrosInactivos() {
        carrito.agregarLibro(libro, 2);
        libro.setActivo(false);
        conCarritoExistente();

        servicio.obtener(actual);

        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("al consultar el carrito se actualizan los precios si el vendedor los cambió")
    void obtener_sincronizaPrecios() {
        carrito.agregarLibro(libro, 2);
        libro.setPrecio(new BigDecimal("12.00"));
        conCarritoExistente();

        servicio.obtener(actual);

        assertThat(carrito.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo("12.00");
        assertThat(carrito.getTotal()).isEqualByComparingTo("24.00");
    }

    @Test
    @DisplayName("actualizar la cantidad dentro del stock recalcula el total")
    void actualizarCantidad_ok() {
        carrito.agregarLibro(libro, 1);
        carrito.getDetalles().get(0).setIdDetalle(100L);
        conCarritoExistente();

        servicio.actualizarCantidad(actual, 100L, new CarritoCantidadRequest(3));

        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(3);
        assertThat(carrito.getTotal()).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("actualizar a una cantidad mayor que el stock: 409 y no cambia")
    void actualizarCantidad_excedeStock() {
        carrito.agregarLibro(libro, 1);
        carrito.getDetalles().get(0).setIdDetalle(100L);
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.CONFLICT, () -> servicio.actualizarCantidad(actual, 100L, new CarritoCantidadRequest(4)));

        assertThat(carrito.getDetalles().get(0).getCantidad()).isEqualTo(1);
    }

    @Test
    @DisplayName("una línea que no está en MI carrito responde 404 (nadie toca el carrito de otro)")
    void actualizarCantidad_lineaAjena() {
        carrito.agregarLibro(libro, 1);
        carrito.getDetalles().get(0).setIdDetalle(100L);
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.actualizarCantidad(actual, 999L, new CarritoCantidadRequest(1)));
    }

    @Test
    @DisplayName("eliminar una línea la quita del carrito")
    void eliminarLibro_ok() {
        carrito.agregarLibro(libro, 2);
        carrito.getDetalles().get(0).setIdDetalle(100L);
        conCarritoExistente();

        servicio.eliminarLibro(actual, 100L);

        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("eliminar una línea que no existe responde 404")
    void eliminarLibro_noExiste() {
        when(carritos.findByUsuario_IdUsuario(5L)).thenReturn(Optional.of(carrito));

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.eliminarLibro(actual, 999L));
    }

    @Test
    @DisplayName("vaciar deja el carrito sin líneas y con total 0")
    void vaciar() {
        carrito.agregarLibro(libro, 2);
        conCarritoExistente();

        servicio.vaciar(actual);

        assertThat(carrito.getDetalles()).isEmpty();
        assertThat(carrito.getTotal()).isEqualByComparingTo("0");
        verify(carritos).saveAndFlush(carrito);
    }
}
