package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CategoriaRequest;
import com.novalectura.novalectura_backend.dto.response.CategoriaResponse;
import com.novalectura.novalectura_backend.entity.Categoria;
import com.novalectura.novalectura_backend.mapper.CategoriaMapper;
import com.novalectura.novalectura_backend.repository.CategoriaRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService")
class CategoriaServiceTest {
    @Mock CategoriaRepository categorias;
    @Mock LibroRepository libros;
    @Mock CategoriaMapper mapper;
    @InjectMocks CategoriaService servicio;

    @Test
    @DisplayName("listar devuelve las categorías ordenadas por nombre")
    void listar() {
        Categoria a = Datos.categoria(1L, "Ciencia");
        Categoria b = Datos.categoria(2L, "Historia");
        CategoriaResponse ra = new CategoriaResponse(1L, "Ciencia", null);
        CategoriaResponse rb = new CategoriaResponse(2L, "Historia", null);
        when(categorias.findAll(Sort.by("nombre"))).thenReturn(List.of(a, b));
        when(mapper.toResponse(a)).thenReturn(ra);
        when(mapper.toResponse(b)).thenReturn(rb);

        assertThat(servicio.listar()).containsExactly(ra, rb);
    }

    @Test
    @DisplayName("crear guarda la categoría con el nombre sin espacios sobrantes")
    void crear_ok() {
        CategoriaRequest request = new CategoriaRequest("  Fantasía ", "Magia y dragones");
        Categoria entidad = new Categoria();
        when(categorias.existsByNombreIgnoreCase("Fantasía")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entidad);
        when(categorias.save(entidad)).thenReturn(entidad);
        when(mapper.toResponse(entidad)).thenReturn(new CategoriaResponse(1L, "Fantasía", "Magia y dragones"));

        CategoriaResponse respuesta = servicio.crear(request);

        assertThat(entidad.getNombre()).isEqualTo("Fantasía");
        assertThat(respuesta.nombre()).isEqualTo("Fantasía");
    }

    @Test
    @DisplayName("crear con un nombre existente (sin importar mayúsculas) responde 409")
    void crear_duplicada() {
        when(categorias.existsByNombreIgnoreCase("Fantasía")).thenReturn(true);

        assertApi(HttpStatus.CONFLICT, () -> servicio.crear(new CategoriaRequest("Fantasía", null)));

        verify(categorias, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("actualizar una categoría inexistente responde 404")
    void actualizar_noExiste() {
        when(categorias.findById(9L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.actualizar(9L, new CategoriaRequest("X", null)));
    }

    @Test
    @DisplayName("actualizar con el nombre de OTRA categoría responde 409")
    void actualizar_nombreEnUso() {
        when(categorias.findById(1L)).thenReturn(Optional.of(Datos.categoria(1L, "Ciencia")));
        when(categorias.existsByNombreIgnoreCaseAndIdCategoriaNot("Historia", 1L)).thenReturn(true);

        assertApi(HttpStatus.CONFLICT, () -> servicio.actualizar(1L, new CategoriaRequest("Historia", null)));
    }

    @Test
    @DisplayName("actualizar permite conservar el propio nombre y aplica los cambios")
    void actualizar_ok() {
        Categoria existente = Datos.categoria(1L, "Ciencia");
        CategoriaRequest request = new CategoriaRequest("  Ciencia ", "Nueva descripción");
        when(categorias.findById(1L)).thenReturn(Optional.of(existente));
        when(categorias.existsByNombreIgnoreCaseAndIdCategoriaNot("Ciencia", 1L)).thenReturn(false);
        when(mapper.toResponse(existente)).thenReturn(new CategoriaResponse(1L, "Ciencia", "Nueva descripción"));

        servicio.actualizar(1L, request);

        verify(mapper).actualizar(request, existente);
        assertThat(existente.getNombre()).isEqualTo("Ciencia");
    }

    @Test
    @DisplayName("eliminar una categoría inexistente responde 404")
    void eliminar_noExiste() {
        when(categorias.findById(9L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.eliminar(9L));
    }

    @Test
    @DisplayName("no se puede eliminar una categoría que tiene libros: 409 y no se borra")
    void eliminar_conLibros() {
        Categoria categoria = Datos.categoria(1L, "Ciencia");
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(libros.existsByCategoria_IdCategoria(1L)).thenReturn(true);

        assertApi(HttpStatus.CONFLICT, () -> servicio.eliminar(1L));

        verify(categorias, never()).delete(any(Categoria.class));
    }

    @Test
    @DisplayName("una categoría sin libros se elimina")
    void eliminar_ok() {
        Categoria categoria = Datos.categoria(1L, "Ciencia");
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria));
        when(libros.existsByCategoria_IdCategoria(1L)).thenReturn(false);

        servicio.eliminar(1L);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categorias).delete(captor.capture());
        assertThat(captor.getValue()).isSameAs(categoria);
    }
}
