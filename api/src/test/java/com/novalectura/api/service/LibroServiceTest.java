package com.novalectura.api.service;

import com.novalectura.api.dto.LibroRequest;
import com.novalectura.api.dto.LibroResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.*;
import com.novalectura.api.repository.CategoriaRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    private static final String CORREO_VENDEDOR = "ana@mail.com";
    private static final String CORREO_OTRO = "otro@mail.com";
    private static final String CORREO_ADMIN = "admin@mail.com";

    @Mock LibroRepository libroRepository;
    @Mock CategoriaRepository categoriaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock AlmacenImagenes almacenImagenes;
    @Mock ApplicationEventPublisher eventos;
    @InjectMocks LibroService libroService;

    private final Categoria categoria = Categoria.builder().id(1L).nombre("Literatura").build();
    private final Usuario vendedor = Usuario.builder().id(1L).nombres("Ana").apellidos("Paz")
            .correo(CORREO_VENDEDOR).rol(Rol.CLIENTE).build();
    private final Usuario otro = Usuario.builder().id(2L).nombres("Luis").apellidos("Gil")
            .correo(CORREO_OTRO).rol(Rol.CLIENTE).build();
    private final Usuario admin = Usuario.builder().id(3L).nombres("Admin").apellidos("Nova")
            .correo(CORREO_ADMIN).rol(Rol.ADMINISTRADOR).build();

    @BeforeEach
    void preparar() {
        lenient().when(usuarioRepository.findByCorreo(CORREO_VENDEDOR)).thenReturn(Optional.of(vendedor));
        lenient().when(usuarioRepository.findByCorreo(CORREO_OTRO)).thenReturn(Optional.of(otro));
        lenient().when(usuarioRepository.findByCorreo(CORREO_ADMIN)).thenReturn(Optional.of(admin));
    }

    private Libro libro(Long id) {
        return Libro.builder().id(id).titulo("Don Quijote").autor("Cervantes").precio(new BigDecimal("15.50"))
                .stock(3).condicion(CondicionLibro.BUENO).categoria(categoria).vendedor(vendedor).build();
    }

    private LibroRequest solicitud() {
        return new LibroRequest("Dune", "Frank Herbert", "Chilton", "9780441013593", 1965, "Ciencia ficción",
                new BigDecimal("24.99"), 4, "http://img/dune.jpg", CondicionLibro.MUY_BUENO, 1L);
    }

    @Test
    void listar_normalizaElTerminoDeBusqueda() {
        Pageable pagina = PageRequest.of(0, 12);
        Page<Libro> resultado = new PageImpl<>(List.of(libro(1L)), pagina, 1);
        when(libroRepository.buscar(eq("don quijote"), isNull(), eq(pagina))).thenReturn(resultado);

        Page<LibroResponse> respuesta = libroService.listar("  Don QUIJOTE ", null, pagina);

        assertThat(respuesta.getContent()).hasSize(1);
        assertThat(respuesta.getContent().getFirst().titulo()).isEqualTo("Don Quijote");
    }

    @Test
    void listar_sinTermino_buscaConCadenaVacia() {
        Pageable pagina = PageRequest.of(0, 12);
        when(libroRepository.buscar(eq(""), eq(1L), eq(pagina))).thenReturn(Page.empty(pagina));

        assertThat(libroService.listar(null, 1L, pagina).getContent()).isEmpty();
    }

    @Test
    void obtener_libroActivo_devuelveElDetalleConSuVendedor() {
        when(libroRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(libro(1L)));

        LibroResponse respuesta = libroService.obtener(1L);

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.vendedorId()).isEqualTo(1L);
        assertThat(respuesta.vendedorNombre()).isEqualTo("Ana Paz");
    }

    @Test
    void obtener_libroInexistenteOInactivo_lanzaNoEncontrado() {
        when(libroRepository.findByIdAndActivoTrue(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.obtener(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void listarMisLibros_devuelveSoloLosLibrosActivosDelVendedor() {
        when(libroRepository.findByVendedorIdAndActivoTrueOrderByIdDesc(1L)).thenReturn(List.of(libro(1L)));

        List<LibroResponse> resultado = libroService.listarMisLibros(CORREO_VENDEDOR);

        assertThat(resultado).extracting(LibroResponse::id).containsExactly(1L);
    }

    @Test
    void crear_asignaComoVendedorAlUsuarioAutenticado() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(any(Libro.class))).thenAnswer(i -> i.getArgument(0));

        LibroResponse respuesta = libroService.crear(CORREO_OTRO, solicitud());

        assertThat(respuesta.titulo()).isEqualTo("Dune");
        assertThat(respuesta.stock()).isEqualTo(4);
        assertThat(respuesta.condicion()).isEqualTo(CondicionLibro.MUY_BUENO);
        assertThat(respuesta.categoria().id()).isEqualTo(1L);
        assertThat(respuesta.vendedorId()).isEqualTo(2L);
    }

    @Test
    void crear_categoriaInexistente_lanzaNoEncontradoSinGuardar() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.crear(CORREO_OTRO, solicitud()))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(libroRepository, never()).save(any());
    }

    @Test
    void actualizar_elVendedor_reemplazaLosDatosDeSuLibro() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(existente)).thenReturn(existente);

        LibroResponse respuesta = libroService.actualizar(5L, CORREO_VENDEDOR, solicitud());

        assertThat(respuesta.titulo()).isEqualTo("Dune");
        assertThat(existente.getPrecio()).isEqualByComparingTo("24.99");
        assertThat(existente.getStock()).isEqualTo(4);
    }

    @Test
    void actualizar_conOtraImagen_eliminaLaImagenAnteriorDelAlmacen() {
        Libro existente = libro(5L);
        existente.setImagen("/api/imagenes/vieja.png");
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(existente)).thenReturn(existente);

        libroService.actualizar(5L, CORREO_VENDEDOR, solicitud());

        verify(almacenImagenes).eliminar("/api/imagenes/vieja.png");
    }

    @Test
    void actualizar_conLaMismaImagen_noLaElimina() {
        Libro existente = libro(5L);
        existente.setImagen("http://img/dune.jpg");
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(existente)).thenReturn(existente);

        libroService.actualizar(5L, CORREO_VENDEDOR, solicitud());

        verify(almacenImagenes, never()).eliminar(any());
    }

    @Test
    void actualizar_otroUsuario_lanzaAccesoDenegado() {
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(libro(5L)));

        assertThatThrownBy(() -> libroService.actualizar(5L, CORREO_OTRO, solicitud()))
                .isInstanceOf(AccessDeniedException.class);

        verify(libroRepository, never()).save(any());
    }

    @Test
    void actualizar_administrador_puedeEditarUnLibroAjenoSinCambiarSuVendedor() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(existente)).thenReturn(existente);

        LibroResponse respuesta = libroService.actualizar(5L, CORREO_ADMIN, solicitud());

        assertThat(respuesta.vendedorId()).isEqualTo(1L);
    }

    @Test
    void actualizar_libroInexistente_lanzaNoEncontrado() {
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.actualizar(5L, CORREO_VENDEDOR, solicitud()))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminar_elVendedor_hacerBorradoLogico() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));

        libroService.eliminar(5L, CORREO_VENDEDOR);

        assertThat(existente.isActivo()).isFalse();
        verify(libroRepository).save(existente);
        verify(libroRepository, never()).delete(any());
    }

    @Test
    void eliminar_otroUsuario_lanzaAccesoDenegado() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> libroService.eliminar(5L, CORREO_OTRO))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(existente.isActivo()).isTrue();
    }

    @Test
    void eliminar_administrador_puedeEliminarUnLibroAjeno() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));

        libroService.eliminar(5L, CORREO_ADMIN);

        assertThat(existente.isActivo()).isFalse();
    }

    @Test
    void eliminar_libroInexistente_lanzaNoEncontrado() {
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> libroService.eliminar(5L, CORREO_VENDEDOR))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void crear_avisaQueElCatalogoCambio() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(any(Libro.class))).thenAnswer(i -> i.getArgument(0));

        libroService.crear(CORREO_OTRO, solicitud());

        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
    }

    @Test
    void actualizar_avisaQueElCatalogoCambio() {
        Libro existente = libro(5L);
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.save(existente)).thenReturn(existente);

        libroService.actualizar(5L, CORREO_VENDEDOR, solicitud());

        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
    }

    @Test
    void eliminar_avisaQueElCatalogoCambio() {
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(libro(5L)));

        libroService.eliminar(5L, CORREO_VENDEDOR);

        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
    }

    @Test
    void siLaOperacionFalla_noAvisaDeCambios() {
        when(libroRepository.findByIdAndActivoTrue(5L)).thenReturn(Optional.of(libro(5L)));

        assertThatThrownBy(() -> libroService.eliminar(5L, CORREO_OTRO)).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(eventos);
    }
}
