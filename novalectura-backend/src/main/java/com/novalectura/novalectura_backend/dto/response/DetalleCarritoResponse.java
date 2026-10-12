package com.novalectura.novalectura_backend.dto.response;

import java.math.BigDecimal;

public record DetalleCarritoResponse(
        Long idDetalle,
        Long idLibro,
        String titulo,
        String imagen,
        Integer stockDisponible,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
