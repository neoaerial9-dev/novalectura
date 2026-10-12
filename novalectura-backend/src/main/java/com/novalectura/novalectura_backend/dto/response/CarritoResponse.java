package com.novalectura.novalectura_backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(
        Long idCarrito,
        BigDecimal total,
        List<DetalleCarritoResponse> detalles) {
}
