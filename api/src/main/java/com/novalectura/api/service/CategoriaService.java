package com.novalectura.api.service;

import com.novalectura.api.dto.CategoriaRequest;
import com.novalectura.api.dto.CategoriaResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.Categoria;
import com.novalectura.api.repository.CategoriaRepository;
import com.novalectura.api.repository.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest solicitud) {
        String nombre = solicitud.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una categoría con el nombre '" + nombre + "'");
        }
        Categoria categoria = Categoria.builder().nombre(nombre).descripcion(solicitud.descripcion()).build();
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest solicitud) {
        Categoria categoria = buscar(id);
        String nombre = solicitud.nombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe una categoría con el nombre '" + nombre + "'");
        }
        categoria.setNombre(nombre);
        categoria.setDescripcion(solicitud.descripcion());
        return CategoriaResponse.from(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (libroRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar una categoría que tiene libros asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }
}
