package com.novalectura.novalectura_backend.repository;

import com.novalectura.novalectura_backend.entity.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    @Query("""
            SELECT l FROM Libro l JOIN l.categoria c
            WHERE l.activo = true AND l.stock > 0
              AND (LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:idCategoria IS NULL OR c.idCategoria = :idCategoria)
            """)
    Page<Libro> buscarEnCatalogo(@Param("q") String q,
                                 @Param("idCategoria") Long idCategoria,
                                 Pageable pageable);

    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    Optional<Libro> findByIdLibroAndActivoTrue(Long idLibro);

    @EntityGraph(attributePaths = {"categoria", "vendedor"})
    Page<Libro> findByVendedor_IdUsuarioAndActivoTrue(Long idVendedor, Pageable pageable);

    boolean existsByCategoria_IdCategoria(Long idCategoria);
}
