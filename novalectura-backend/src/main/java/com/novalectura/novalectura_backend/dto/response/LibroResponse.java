package com.novalectura.novalectura_backend.dto.response;

import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LibroResponse(
        Long idLibro,
        String titulo,
        String autor,
        BigDecimal precio,
        Integer stock,
        String descripcion,
        String imagen,
        CondicionLibro condicion,
        LocalDateTime fechaPublicacion,
        CategoriaResponse categoria,
        VendedorResumen vendedor) {
}
