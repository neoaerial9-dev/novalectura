package com.novalectura.api.dto;

import com.novalectura.api.model.DetalleCarrito;

import java.math.BigDecimal;

public record ItemCarritoResponse(Long libroId, String titulo, String imagen, BigDecimal precioUnitario,
                                  int cantidad, BigDecimal subtotal, int stockDisponible,
                                  boolean disponible) {

    public static ItemCarritoResponse from(DetalleCarrito d) {
        return new ItemCarritoResponse(d.getLibro().getId(), d.getLibro().getTitulo(), d.getLibro().getImagen(),
                d.getLibro().getPrecio(), d.getCantidad(), d.calcularSubtotal(), d.getLibro().getStock(),
                d.getLibro().isActivo());
    }
}
