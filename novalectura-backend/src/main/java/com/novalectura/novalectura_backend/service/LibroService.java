package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.entity.Categoria;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.LibroMapper;
import com.novalectura.novalectura_backend.repository.CategoriaRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LibroService {
    private static final int TAMANO_MAXIMO = 50;

    private final LibroRepository libros;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final LibroMapper mapper;

    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> buscar(String q, Long idCategoria,
                                              int pagina, int tamano, String orden) {
        String texto = (q == null) ? "" : q.trim();
        Pageable pageable = crearPageable(pagina, tamano, ordenCatalogo(orden));
        return PageResponse.de(libros.buscarEnCatalogo(texto, idCategoria, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long idLibro) {
        return mapper.toResponse(buscarActivo(idLibro));
    }

    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> misLibros(UsuarioDetails actual, int pagina, int tamano) {
        Pageable pageable = crearPageable(pagina, tamano, ordenCatalogo(null));
        return PageResponse.de(
                libros.findByVendedor_IdUsuarioAndActivoTrue(actual.getIdUsuario(), pageable)
                        .map(mapper::toResponse));
    }

    @Transactional
    public LibroResponse crear(UsuarioDetails actual, LibroRequest request) {
        Libro libro = mapper.toEntity(request);
        libro.setCategoria(obtenerCategoria(request.idCategoria()));
        libro.setVendedor(usuarios.getReferenceById(actual.getIdUsuario()));
        return mapper.toResponse(libros.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long idLibro, UsuarioDetails actual, LibroRequest request) {
        Libro libro = buscarActivo(idLibro);
        verificarPropietarioOAdmin(libro, actual);

        mapper.actualizar(request, libro);
        if (!libro.getCategoria().getIdCategoria().equals(request.idCategoria())) {
            libro.setCategoria(obtenerCategoria(request.idCategoria()));
        }
        return mapper.toResponse(libro);
    }

    @Transactional
    public void eliminar(Long idLibro, UsuarioDetails actual) {
        Libro libro = buscarActivo(idLibro);
        verificarPropietarioOAdmin(libro, actual);
        libro.setActivo(false);
    }

    private Libro buscarActivo(Long idLibro) {
        return libros.findByIdLibroAndActivoTrue(idLibro)
                .orElseThrow(() -> ApiException.noEncontrado("Libro no encontrado"));
    }

    private Categoria obtenerCategoria(Long idCategoria) {
        return categorias.findById(idCategoria)
                .orElseThrow(() -> ApiException.solicitudInvalida("La categoría indicada no existe"));
    }

    private void verificarPropietarioOAdmin(Libro libro, UsuarioDetails actual) {
        boolean esPropietario = libro.getVendedor().getIdUsuario().equals(actual.getIdUsuario());
        if (!esPropietario && !actual.esAdmin()) {
            throw ApiException.prohibido("Solo puedes modificar tus propios libros");
        }
    }

    private Sort ordenCatalogo(String orden) {
        Sort desempate = Sort.by(Sort.Direction.DESC, "idLibro");
        if (orden == null) {
            return Sort.by(Sort.Direction.DESC, "fechaPublicacion").and(desempate);
        }
        return switch (orden) {
            case "precio_asc" -> Sort.by(Sort.Direction.ASC, "precio").and(desempate);
            case "precio_desc" -> Sort.by(Sort.Direction.DESC, "precio").and(desempate);
            default -> Sort.by(Sort.Direction.DESC, "fechaPublicacion").and(desempate);
        };
    }

    private Pageable crearPageable(int pagina, int tamano, Sort sort) {
        int paginaSegura = Math.max(pagina, 0);
        int tamanoSeguro = Math.min(Math.max(tamano, 1), TAMANO_MAXIMO);
        return PageRequest.of(paginaSegura, tamanoSeguro, sort);
    }
}
