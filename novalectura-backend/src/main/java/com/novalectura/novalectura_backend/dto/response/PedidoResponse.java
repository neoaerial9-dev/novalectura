package com.novalectura.novalectura_backend.dto.response;

import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long idPedido,
        LocalDateTime fechaPedido,
        BigDecimal total,
        EstadoPedido estado,
        CompradorResumen comprador,
        List<DetallePedidoResponse> detalles) {
}
