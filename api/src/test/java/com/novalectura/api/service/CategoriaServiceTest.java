package com.novalectura.api.service;

import com.novalectura.api.dto.CategoriaRequest;
import com.novalectura.api.dto.CategoriaResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.Categoria;
import com.novalectura.api.repository.CategoriaRepository;
import com.novalectura.api.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock LibroRepository libroRepository;
    @InjectMocks CategoriaService categoriaService;

    @Test
    void listar_devuelveCategoriasOrdenadas() {
        when(categoriaRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(
                Categoria.builder().id(1L).nombre("Ciencia").build(),
                Categoria.builder().id(2L).nombre("Historia").build()));

        List<CategoriaResponse> resultado = categoriaService.listar();

        assertThat(resultado).extracting(CategoriaResponse::nombre).containsExactly("Ciencia", "Historia");
    }

    @Test
    void crear_nombreNuevo_guardaLaCategoria() {
        when(categoriaRepository.existsByNombreIgnoreCase("Literatura")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(i -> i.getArgument(0));

        CategoriaResponse respuesta = categoriaService.crear(new CategoriaRequest(" Literatura ", "Novelas"));

        assertThat(respuesta.nombre()).isEqualTo("Literatura");
        assertThat(respuesta.descripcion()).isEqualTo("Novelas");
    }

    @Test
    void crear_nombreRepetido_lanzaConflicto() {
        when(categoriaRepository.existsByNombreIgnoreCase("Literatura")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.crear(new CategoriaRequest("Literatura", null)))
                .isInstanceOf(ConflictoException.class);

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizar_modificaNombreYDescripcion() {
        Categoria existente = Categoria.builder().id(1L).nombre("Cienca").build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Ciencia", 1L)).thenReturn(false);
        when(categoriaRepository.save(existente)).thenReturn(existente);

        CategoriaResponse respuesta = categoriaService.actualizar(1L, new CategoriaRequest("Ciencia", "Libros de ciencia"));

        assertThat(respuesta.nombre()).isEqualTo("Ciencia");
        assertThat(existente.getDescripcion()).isEqualTo("Libros de ciencia");
    }

    @Test
    void actualizar_nombreDeOtraCategoria_lanzaConflicto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().id(1L).nombre("A").build()));
        when(categoriaRepository.existsByNombreIgnoreCaseAndIdNot("Historia", 1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.actualizar(1L, new CategoriaRequest("Historia", null)))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void actualizar_inexistente_lanzaNoEncontrado() {
        when(categoriaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.actualizar(9L, new CategoriaRequest("X", null)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminar_sinLibros_laElimina() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Ciencia").build();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(libroRepository.existsByCategoriaId(1L)).thenReturn(false);

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminar_conLibrosAsociados_lanzaConflicto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(Categoria.builder().id(1L).build()));
        when(libroRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.eliminar(1L)).isInstanceOf(ConflictoException.class);

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminar_inexistente_lanzaNoEncontrado() {
        when(categoriaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.eliminar(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }
}
