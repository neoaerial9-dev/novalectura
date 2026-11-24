package com.novalectura.api.dto;

import com.novalectura.api.model.CondicionLibro;
import com.novalectura.api.model.Libro;

import java.math.BigDecimal;

public record LibroResponse(Long id, String titulo, String autor, String editorial, String isbn,
                            Integer anioPublicacion, String descripcion, BigDecimal precio, int stock,
                            String imagen, CondicionLibro condicion, CategoriaResponse categoria,
                            Long vendedorId, String vendedorNombre) {

    public static LibroResponse from(Libro l) {
        return new LibroResponse(l.getId(), l.getTitulo(), l.getAutor(), l.getEditorial(), l.getIsbn(),
                l.getAnioPublicacion(), l.getDescripcion(), l.getPrecio(), l.getStock(), l.getImagen(),
                l.getCondicion(), CategoriaResponse.from(l.getCategoria()),
                l.getVendedor().getId(),
                l.getVendedor().getNombres() + " " + l.getVendedor().getApellidos());
    }
}
