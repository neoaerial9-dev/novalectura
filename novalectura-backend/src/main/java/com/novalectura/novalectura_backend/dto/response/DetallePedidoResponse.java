package com.novalectura.novalectura_backend.dto.response;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Long idLibro,
        String titulo,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        VendedorResumen vendedor) {
}
