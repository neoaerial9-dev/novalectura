package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CategoriaRequest;
import com.novalectura.novalectura_backend.dto.response.CategoriaResponse;
import com.novalectura.novalectura_backend.entity.Categoria;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.CategoriaMapper;
import com.novalectura.novalectura_backend.repository.CategoriaRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final LibroRepository libros;
    private final CategoriaMapper mapper;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAll(Sort.by("nombre")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.nombre().trim();
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw ApiException.conflicto("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = mapper.toEntity(request);
        categoria.setNombre(nombre);
        return mapper.toResponse(categorias.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long idCategoria, CategoriaRequest request) {
        Categoria categoria = buscar(idCategoria);
        String nombre = request.nombre().trim();
        if (categorias.existsByNombreIgnoreCaseAndIdCategoriaNot(nombre, idCategoria)) {
            throw ApiException.conflicto("Ya existe otra categoría con ese nombre");
        }
        mapper.actualizar(request, categoria);
        categoria.setNombre(nombre);
        return mapper.toResponse(categoria);
    }

    @Transactional
    public void eliminar(Long idCategoria) {
        Categoria categoria = buscar(idCategoria);
        if (libros.existsByCategoria_IdCategoria(idCategoria)) {
            throw ApiException.conflicto("No se puede eliminar: la categoría tiene libros asociados");
        }
        categorias.delete(categoria);
    }

    private Categoria buscar(Long idCategoria) {
        return categorias.findById(idCategoria)
                .orElseThrow(() -> ApiException.noEncontrado("Categoría no encontrada"));
    }
}
