package com.novalectura.api.service;

import com.novalectura.api.dto.LibroRequest;
import com.novalectura.api.dto.LibroResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.Categoria;
import com.novalectura.api.model.Libro;
import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.CategoriaRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenImagenes almacenImagenes;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public Page<LibroResponse> listar(String termino, Long categoriaId, Pageable pageable) {
        String q = termino == null ? "" : termino.trim().toLowerCase();
        return libroRepository.buscar(q, categoriaId, pageable).map(LibroResponse::from);
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.from(buscarActivo(id));
    }

    @Transactional(readOnly = true)
    public List<LibroResponse> listarMisLibros(String correo) {
        Usuario vendedor = buscarUsuario(correo);
        return libroRepository.findByVendedorIdAndActivoTrueOrderByIdDesc(vendedor.getId())
                .stream().map(LibroResponse::from).toList();
    }

    @Transactional
    public LibroResponse crear(String correo, LibroRequest solicitud) {
        Usuario vendedor = buscarUsuario(correo);
        Categoria categoria = buscarCategoria(solicitud.categoriaId());
        Libro libro = new Libro();
        libro.setVendedor(vendedor);
        aplicar(libro, solicitud, categoria);
        LibroResponse respuesta = LibroResponse.from(libroRepository.save(libro));
        eventos.publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
        return respuesta;
    }

    @Transactional
    public LibroResponse actualizar(Long id, String correo, LibroRequest solicitud) {
        Libro libro = buscarActivo(id);
        verificarPermiso(libro, buscarUsuario(correo));
        Categoria categoria = buscarCategoria(solicitud.categoriaId());
        String imagenAnterior = libro.getImagen();
        aplicar(libro, solicitud, categoria);
        LibroResponse respuesta = LibroResponse.from(libroRepository.save(libro));
        if (imagenAnterior != null && !imagenAnterior.equals(libro.getImagen())) {
            almacenImagenes.eliminar(imagenAnterior);
        }
        eventos.publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
        return respuesta;
    }

    @Transactional
    public void eliminar(Long id, String correo) {
        Libro libro = buscarActivo(id);
        verificarPermiso(libro, buscarUsuario(correo));
        libro.setActivo(false);
        libroRepository.save(libro);
        eventos.publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
    }

    private void verificarPermiso(Libro libro, Usuario usuario) {
        boolean esDueno = libro.getVendedor().getId().equals(usuario.getId());
        if (!esDueno && usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new AccessDeniedException("Solo el vendedor puede modificar este libro");
        }
    }

    private Libro buscarActivo(Long id) {
        return libroRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado"));
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    private void aplicar(Libro libro, LibroRequest s, Categoria categoria) {
        libro.setTitulo(s.titulo().trim());
        libro.setAutor(s.autor().trim());
        libro.setEditorial(s.editorial());
        libro.setIsbn(s.isbn());
        libro.setAnioPublicacion(s.anioPublicacion());
        libro.setDescripcion(s.descripcion());
        libro.setPrecio(s.precio());
        libro.setStock(s.stock());
        libro.setImagen(s.imagen());
        libro.setCondicion(s.condicion());
        libro.setCategoria(categoria);
    }
}
