package com.novalectura.api.repository;

import com.novalectura.api.model.Libro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    Optional<Libro> findByIdAndActivoTrue(Long id);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("select l.imagen from Libro l where l.imagen like '/api/imagenes/%'")
    List<String> findRutasDeImagenesPropias();

    List<Libro> findByVendedorIdAndActivoTrueOrderByIdDesc(Long vendedorId);

    @Query("""
            select l from Libro l
            where l.activo = true
              and (:categoriaId is null or l.categoria.id = :categoriaId)
              and (:q = ''
                   or lower(l.titulo) like concat('%', :q, '%')
                   or lower(l.autor) like concat('%', :q, '%')
                   or lower(l.categoria.nombre) like concat('%', :q, '%'))
            """)
    Page<Libro> buscar(@Param("q") String q, @Param("categoriaId") Long categoriaId, Pageable pageable);

    @Modifying
    @Query("""
            update Libro l set l.stock = l.stock - :cantidad
            where l.id = :id and l.activo = true and l.stock >= :cantidad
            """)
    int descontarStock(@Param("id") Long id, @Param("cantidad") int cantidad);
}
