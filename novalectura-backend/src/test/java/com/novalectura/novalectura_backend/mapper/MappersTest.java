package com.novalectura.novalectura_backend.mapper;

import com.novalectura.novalectura_backend.dto.request.CategoriaRequest;
import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.*;
import com.novalectura.novalectura_backend.entity.*;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        UsuarioMapperImpl.class, CategoriaMapperImpl.class, LibroMapperImpl.class,
        CarritoMapperImpl.class, PedidoMapperImpl.class})
@DisplayName("Mappers (MapStruct)")
class MappersTest {
    @Autowired UsuarioMapper usuarioMapper;
    @Autowired CategoriaMapper categoriaMapper;
    @Autowired LibroMapper libroMapper;
    @Autowired CarritoMapper carritoMapper;
    @Autowired PedidoMapper pedidoMapper;

    private static LibroRequest requestLibro() {
        return new LibroRequest("Dune", "Frank Herbert", new BigDecimal("24.99"), 2,
                "Clásico de ciencia ficción", "dune.jpg", CondicionLibro.MUY_BUENO, 7L);
    }

    @Test
    @DisplayName("UsuarioResponse expone los datos del perfil y NUNCA la contraseña")
    void usuario_noExponeContrasena() {
        Usuario usuario = Datos.usuario(5L, Rol.ADMIN);
        usuario.setTelefono("999888777");
        usuario.setFechaRegistro(LocalDateTime.of(2026, 10, 1, 10, 0));

        UsuarioResponse respuesta = usuarioMapper.toResponse(usuario);

        assertThat(respuesta.correo()).isEqualTo("user5@mail.com");
        assertThat(respuesta.rol()).isEqualTo(Rol.ADMIN);
        assertThat(respuesta.telefono()).isEqualTo("999888777");
        assertThat(Arrays.stream(UsuarioResponse.class.getRecordComponents()).map(c -> c.getName()))
                .noneMatch(nombre -> nombre.toLowerCase().contains("contrasena")
                        || nombre.toLowerCase().contains("password"));
    }

    @Test
    @DisplayName("actualizarPerfil solo toca nombres, apellidos, teléfono y dirección")
    void usuario_actualizarPerfil() {
        Usuario usuario = Datos.usuario(5L);

        usuarioMapper.actualizarPerfil(new UsuarioPerfilRequest("Ana", "Quispe", "123", "Calle 1"), usuario);

        assertThat(usuario.getNombres()).isEqualTo("Ana");
        assertThat(usuario.getApellidos()).isEqualTo("Quispe");
        assertThat(usuario.getTelefono()).isEqualTo("123");
        assertThat(usuario.getDireccion()).isEqualTo("Calle 1");
        assertThat(usuario.getIdUsuario()).isEqualTo(5L);
        assertThat(usuario.getCorreo()).isEqualTo("user5@mail.com");
        assertThat(usuario.getRol()).isEqualTo(Rol.USUARIO);
        assertThat(usuario.getContrasena()).isEqualTo("hash-5");
    }

    @Test
    @DisplayName("Categoría: entidad -> respuesta, request -> entidad y actualización")
    void categoria() {
        CategoriaResponse respuesta = categoriaMapper.toResponse(Datos.categoria(3L, "Historia"));
        assertThat(respuesta.idCategoria()).isEqualTo(3L);
        assertThat(respuesta.nombre()).isEqualTo("Historia");

        Categoria nueva = categoriaMapper.toEntity(new CategoriaRequest("Fantasía", "Magia"));
        assertThat(nueva.getIdCategoria()).as("el id no viene del cliente").isNull();
        assertThat(nueva.getNombre()).isEqualTo("Fantasía");

        Categoria existente = Datos.categoria(3L, "Historia");
        categoriaMapper.actualizar(new CategoriaRequest("Historia universal", "Todo"), existente);
        assertThat(existente.getIdCategoria()).isEqualTo(3L);
        assertThat(existente.getNombre()).isEqualTo("Historia universal");
        assertThat(existente.getDescripcion()).isEqualTo("Todo");
    }

    @Test
    @DisplayName("LibroResponse incluye categoría y un resumen público del vendedor (sin correo ni teléfono)")
    void libro_toResponse() {
        Libro libro = Datos.libro(10L, Datos.usuario(5L), "12.50", 3);
        libro.setDescripcion("Descripción");
        libro.setImagen("img.jpg");
        libro.setFechaPublicacion(LocalDateTime.of(2026, 10, 1, 9, 0));

        LibroResponse respuesta = libroMapper.toResponse(libro);

        assertThat(respuesta.idLibro()).isEqualTo(10L);
        assertThat(respuesta.precio()).isEqualByComparingTo("12.50");
        assertThat(respuesta.condicion()).isEqualTo(CondicionLibro.BUENO);
        assertThat(respuesta.categoria().nombre()).isEqualTo("Ficción");
        assertThat(respuesta.vendedor().idUsuario()).isEqualTo(5L);
        assertThat(respuesta.vendedor().nombreCompleto()).isEqualTo("Nombre5 Apellido5");
    }

    @Test
    @DisplayName("LibroRequest -> entidad: copia los campos editables y deja vendedor, categoría e id sin asignar")
    void libro_toEntity() {
        Libro libro = libroMapper.toEntity(requestLibro());

        assertThat(libro.getTitulo()).isEqualTo("Dune");
        assertThat(libro.getAutor()).isEqualTo("Frank Herbert");
        assertThat(libro.getPrecio()).isEqualByComparingTo("24.99");
        assertThat(libro.getStock()).isEqualTo(2);
        assertThat(libro.getCondicion()).isEqualTo(CondicionLibro.MUY_BUENO);
        assertThat(libro.getIdLibro()).isNull();
        assertThat(libro.getVendedor()).as("lo asigna el servicio").isNull();
        assertThat(libro.getCategoria()).as("lo asigna el servicio").isNull();
        assertThat(libro.isActivo()).isTrue();
    }

    @Test
    @DisplayName("actualizar un libro no cambia su id, vendedor, categoría ni estado activo")
    void libro_actualizar() {
        Usuario vendedor = Datos.usuario(5L);
        Libro libro = Datos.libro(10L, vendedor, "12.50", 3);
        Categoria categoria = libro.getCategoria();

        libroMapper.actualizar(requestLibro(), libro);

        assertThat(libro.getTitulo()).isEqualTo("Dune");
        assertThat(libro.getPrecio()).isEqualByComparingTo("24.99");
        assertThat(libro.getIdLibro()).isEqualTo(10L);
        assertThat(libro.getVendedor()).isSameAs(vendedor);
        assertThat(libro.getCategoria()).isSameAs(categoria);
        assertThat(libro.isActivo()).isTrue();
    }

    @Test
    @DisplayName("CarritoResponse: cada línea trae título, imagen y el stock disponible del libro")
    void carrito() {
        Libro libro = Datos.libro(10L, Datos.usuario(9L), "10.00", 4);
        libro.setImagen("portada.jpg");
        Carrito carrito = new Carrito();
        carrito.setIdCarrito(1L);
        carrito.setUsuario(Datos.usuario(5L));
        carrito.agregarLibro(libro, 2);
        carrito.getDetalles().get(0).setIdDetalle(100L);

        CarritoResponse respuesta = carritoMapper.toResponse(carrito);

        assertThat(respuesta.total()).isEqualByComparingTo("20.00");
        assertThat(respuesta.detalles()).hasSize(1);
        var linea = respuesta.detalles().get(0);
        assertThat(linea.idDetalle()).isEqualTo(100L);
        assertThat(linea.idLibro()).isEqualTo(10L);
        assertThat(linea.titulo()).isEqualTo("Libro 10");
        assertThat(linea.imagen()).isEqualTo("portada.jpg");
        assertThat(linea.stockDisponible()).isEqualTo(4);
        assertThat(linea.cantidad()).isEqualTo(2);
        assertThat(linea.subtotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("PedidoResponse incluye al comprador y, en cada línea, al vendedor del libro")
    void pedido() {
        Libro libro = Datos.libro(10L, Datos.usuario(9L), "10.00", 4);
        Carrito carrito = new Carrito();
        carrito.setUsuario(Datos.usuario(5L));
        carrito.agregarLibro(libro, 2);
        Pedido pedido = Pedido.registrarPedido(carrito);
        pedido.setIdPedido(1L);

        PedidoResponse respuesta = pedidoMapper.toResponse(pedido);

        assertThat(respuesta.idPedido()).isEqualTo(1L);
        assertThat(respuesta.total()).isEqualByComparingTo("20.00");
        assertThat(respuesta.comprador().idUsuario()).isEqualTo(5L);
        assertThat(respuesta.comprador().nombreCompleto()).isEqualTo("Nombre5 Apellido5");
        assertThat(respuesta.comprador().correo()).isEqualTo("user5@mail.com");
        assertThat(respuesta.detalles()).hasSize(1);
        var linea = respuesta.detalles().get(0);
        assertThat(linea.titulo()).isEqualTo("Libro 10");
        assertThat(linea.cantidad()).isEqualTo(2);
        assertThat(linea.precioUnitario()).isEqualByComparingTo("10.00");
        assertThat(linea.vendedor().nombreCompleto()).isEqualTo("Nombre9 Apellido9");
    }
}
