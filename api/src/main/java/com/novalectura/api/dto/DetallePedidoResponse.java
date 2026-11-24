package com.novalectura.api.dto;

import com.novalectura.api.model.DetallePedido;

import java.math.BigDecimal;

public record DetallePedidoResponse(Long libroId, String titulo, int cantidad,
                                    BigDecimal precioUnitario, BigDecimal subtotal) {

    public static DetallePedidoResponse from(DetallePedido d) {
        return new DetallePedidoResponse(d.getLibro().getId(), d.getLibro().getTitulo(), d.getCantidad(),
                d.getPrecioUnitario(), d.getSubtotal());
    }
}
