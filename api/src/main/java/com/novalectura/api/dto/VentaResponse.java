package com.novalectura.api.dto;

import com.novalectura.api.model.DetallePedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResponse(Long pedidoId, LocalDateTime fechaPedido, Long libroId, String titulo, int cantidad,
                            BigDecimal precioUnitario, BigDecimal subtotal, String comprador, String correoComprador) {

    public static VentaResponse from(DetallePedido d) {
        return new VentaResponse(d.getPedido().getId(), d.getPedido().getFechaPedido(), d.getLibro().getId(),
                d.getLibro().getTitulo(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal(),
                d.getPedido().getUsuario().getNombres() + " " + d.getPedido().getUsuario().getApellidos(),
                d.getPedido().getUsuario().getCorreo());
    }
}
