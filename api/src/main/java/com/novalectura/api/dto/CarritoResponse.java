package com.novalectura.api.dto;

import com.novalectura.api.model.Carrito;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(List<ItemCarritoResponse> items, BigDecimal total) {

    public static CarritoResponse from(Carrito carrito) {
        return new CarritoResponse(
                carrito.getDetalles().stream().map(ItemCarritoResponse::from).toList(),
                carrito.calcularTotal());
    }
}
