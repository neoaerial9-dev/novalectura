package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.entity.Categoria;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.mapper.LibroMapper;
import com.novalectura.novalectura_backend.repository.CategoriaRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import java.util.List;
import java.util.Optional;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibroService")
class LibroServiceTest {
    @Mock LibroRepository libros;
    @Mock CategoriaRepository categorias;
    @Mock UsuarioRepository usuarios;
    @Mock LibroMapper mapper;
    @InjectMocks LibroService servicio;

    private Usuario vendedor;
    private UsuarioDetails comoVendedor;
    private Libro libro;

    @BeforeEach
    void iniciar() {
        vendedor = Datos.usuario(5L);
        comoVendedor = new UsuarioDetails(vendedor);
        libro = Datos.libro(10L, vendedor, "20.00", 3);
    }

    private static LibroRequest request(Long idCategoria) {
        return new LibroRequest("Dune", "Frank Herbert", new BigDecimal("24.99"), 2,
                "Clásico", "dune.jpg", CondicionLibro.BUENO, idCategoria);
    }

    private static LibroResponse respuesta() {
        return new LibroResponse(10L, "Dune", "Frank Herbert", new BigDecimal("24.99"), 2,
                "Clásico", "dune.jpg", CondicionLibro.BUENO, null, null, null);
    }

    private Page<Libro> paginaVacia() {
        return new PageImpl<>(List.of());
    }

    @Test
    @DisplayName("crear: el vendedor es SIEMPRE el usuario autenticado y la categoría se toma de la base de datos")
    void crear_asignaVendedorYCategoria() {
        Libro entidad = new Libro();
        Categoria categoria = Datos.categoria(2L, "Ciencia ficción");
        when(mapper.toEntity(any(LibroRequest.class))).thenReturn(entidad);
        when(categorias.findById(2L)).thenReturn(Optional.of(categoria));
        when(usuarios.getReferenceById(5L)).thenReturn(vendedor);
        when(libros.save(entidad)).thenReturn(entidad);
        when(mapper.toResponse(entidad)).thenReturn(respuesta());

        LibroResponse resultado = servicio.crear(comoVendedor, request(2L));

        ArgumentCaptor<Libro> captor = ArgumentCaptor.forClass(Libro.class);
        verify(libros).save(captor.capture());
        assertThat(captor.getValue().getVendedor()).isSameAs(vendedor);
        assertThat(captor.getValue().getCategoria()).isSameAs(categoria);
        assertThat(resultado.titulo()).isEqualTo("Dune");
    }

    @Test
    @DisplayName("crear con una categoría inexistente responde 400 y no guarda")
    void crear_categoriaInexistente() {
        when(mapper.toEntity(any(LibroRequest.class))).thenReturn(new Libro());
        when(categorias.findById(99L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.BAD_REQUEST, () -> servicio.crear(comoVendedor, request(99L)));

        verify(libros, never()).save(any(Libro.class));
    }

    @Test
    @DisplayName("el dueño puede actualizar su libro (misma categoría: no consulta categorías)")
    void actualizar_dueno() {
        LibroRequest request = request(1L);
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(mapper.toResponse(libro)).thenReturn(respuesta());

        servicio.actualizar(10L, comoVendedor, request);

        verify(mapper).actualizar(request, libro);
        verifyNoInteractions(categorias);
    }

    @Test
    @DisplayName("al cambiar de categoría se busca la nueva y se asigna")
    void actualizar_cambiaCategoria() {
        Categoria nueva = Datos.categoria(3L, "Historia");
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(categorias.findById(3L)).thenReturn(Optional.of(nueva));
        when(mapper.toResponse(libro)).thenReturn(respuesta());

        servicio.actualizar(10L, comoVendedor, request(3L));

        assertThat(libro.getCategoria()).isSameAs(nueva);
    }

    @Test
    @DisplayName("otro usuario NO puede actualizar un libro ajeno: 403 y no se modifica")
    void actualizar_ajeno() {
        UsuarioDetails otro = new UsuarioDetails(Datos.usuario(6L));
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        assertApi(HttpStatus.FORBIDDEN, () -> servicio.actualizar(10L, otro, request(1L)));

        verify(mapper, never()).actualizar(any(LibroRequest.class), any(Libro.class));
    }

    @Test
    @DisplayName("el ADMIN sí puede actualizar libros de otros usuarios")
    void actualizar_admin() {
        UsuarioDetails admin = new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN));
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(mapper.toResponse(libro)).thenReturn(respuesta());

        servicio.actualizar(10L, admin, request(1L));

        verify(mapper).actualizar(any(LibroRequest.class), eq(libro));
    }

    @Test
    @DisplayName("actualizar un libro inexistente o dado de baja responde 404")
    void actualizar_noExiste() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.actualizar(10L, comoVendedor, request(1L)));
    }

    @Test
    @DisplayName("eliminar es una baja lógica: el libro queda inactivo y NO se borra de la base de datos")
    void eliminar_bajaLogica() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        servicio.eliminar(10L, comoVendedor);

        assertThat(libro.isActivo()).isFalse();
        verify(libros, never()).delete(any(Libro.class));
        verify(libros, never()).deleteById(any());
    }

    @Test
    @DisplayName("otro usuario no puede eliminar un libro ajeno: 403 y sigue activo")
    void eliminar_ajeno() {
        UsuarioDetails otro = new UsuarioDetails(Datos.usuario(6L));
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        assertApi(HttpStatus.FORBIDDEN, () -> servicio.eliminar(10L, otro));

        assertThat(libro.isActivo()).isTrue();
    }

    @Test
    @DisplayName("el ADMIN puede eliminar libros ajenos")
    void eliminar_admin() {
        UsuarioDetails admin = new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN));
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));

        servicio.eliminar(10L, admin);

        assertThat(libro.isActivo()).isFalse();
    }

    @Test
    @DisplayName("eliminar un libro inexistente o ya dado de baja responde 404")
    void eliminar_noExiste() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.eliminar(10L, comoVendedor));
    }

    @Test
    @DisplayName("obtener devuelve el detalle de un libro activo")
    void obtener_ok() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.of(libro));
        when(mapper.toResponse(libro)).thenReturn(respuesta());

        assertThat(servicio.obtener(10L).idLibro()).isEqualTo(10L);
    }

    @Test
    @DisplayName("un libro dado de baja o inexistente responde 404")
    void obtener_noExiste() {
        when(libros.findByIdLibroAndActivoTrue(10L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.obtener(10L));
    }

    @Test
    @DisplayName("sin texto de búsqueda se consulta con \"\" (no null) y la página 0 por defecto")
    void buscar_textoNulo() {
        when(libros.buscarEnCatalogo(any(), any(), any(Pageable.class))).thenReturn(paginaVacia());

        PageResponse<LibroResponse> resultado = servicio.buscar(null, null, 0, 12, null);

        verify(libros).buscarEnCatalogo(eq(""), isNull(), any(Pageable.class));
        assertThat(resultado.contenido()).isEmpty();
    }

    @Test
    @DisplayName("el texto de búsqueda se recorta y el filtro de categoría se pasa tal cual")
    void buscar_recortaTexto() {
        when(libros.buscarEnCatalogo(any(), any(), any(Pageable.class))).thenReturn(paginaVacia());

        servicio.buscar("  Dune ", 3L, 0, 12, null);

        verify(libros).buscarEnCatalogo(eq("Dune"), eq(3L), any(Pageable.class));
    }

    @Test
    @DisplayName("la paginación se acota: página mínima 0 y tamaño entre 1 y 50")
    void buscar_acotaPaginacion() {
        when(libros.buscarEnCatalogo(any(), any(), any(Pageable.class))).thenReturn(paginaVacia());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        servicio.buscar("", null, -3, 500, null);
        servicio.buscar("", null, 2, 0, null);

        verify(libros, org.mockito.Mockito.times(2)).buscarEnCatalogo(any(), any(), captor.capture());
        Pageable primera = captor.getAllValues().get(0);
        Pageable segunda = captor.getAllValues().get(1);
        assertThat(primera.getPageNumber()).isZero();
        assertThat(primera.getPageSize()).isEqualTo(50);
        assertThat(segunda.getPageNumber()).isEqualTo(2);
        assertThat(segunda.getPageSize()).isEqualTo(1);
    }

    @ParameterizedTest(name = "orden \"{0}\" ordena por {1} {2}")
    @CsvSource({
            "precio_asc,   precio,           ASC",
            "precio_desc,  precio,           DESC",
            "recientes,    fechaPublicacion, DESC",
            "cualquier,    fechaPublicacion, DESC"
    })
    @DisplayName("el orden solo acepta la lista blanca; cualquier otro valor cae en \"más recientes\"")
    void buscar_orden(String orden, String propiedad, String direccion) {
        when(libros.buscarEnCatalogo(any(), any(), any(Pageable.class))).thenReturn(paginaVacia());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        servicio.buscar("", null, 0, 12, orden);

        verify(libros).buscarEnCatalogo(any(), any(), captor.capture());
        Sort.Order primero = captor.getValue().getSort().iterator().next();
        assertThat(primero.getProperty()).isEqualTo(propiedad);
        assertThat(primero.getDirection()).isEqualTo(Sort.Direction.valueOf(direccion));
        assertThat(captor.getValue().getSort().getOrderFor("idLibro")).as("desempate estable").isNotNull();
    }

    @Test
    @DisplayName("no se puede ordenar por una propiedad arbitraria enviada por el cliente")
    void buscar_ordenArbitrarioIgnorado() {
        when(libros.buscarEnCatalogo(any(), any(), any(Pageable.class))).thenReturn(paginaVacia());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        servicio.buscar("", null, 0, 12, "vendedor.contrasena");

        verify(libros).buscarEnCatalogo(any(), any(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("vendedor.contrasena")).isNull();
    }

    @Test
    @DisplayName("misLibros consulta por el id del usuario autenticado")
    void misLibros() {
        when(libros.findByVendedor_IdUsuarioAndActivoTrue(eq(5L), any(Pageable.class))).thenReturn(paginaVacia());

        servicio.misLibros(comoVendedor, 0, 12);

        verify(libros).findByVendedor_IdUsuarioAndActivoTrue(eq(5L), any(Pageable.class));
    }
}
