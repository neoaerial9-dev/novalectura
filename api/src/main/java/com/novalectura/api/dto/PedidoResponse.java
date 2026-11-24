package com.novalectura.api.dto;

import com.novalectura.api.model.EstadoPedido;
import com.novalectura.api.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, LocalDateTime fechaPedido, BigDecimal total, EstadoPedido estado,
                             String cliente, String correoCliente, List<DetallePedidoResponse> detalles) {

    public static PedidoResponse from(Pedido p) {
        return new PedidoResponse(p.getId(), p.getFechaPedido(), p.getTotal(), p.getEstado(),
                p.getUsuario().getNombres() + " " + p.getUsuario().getApellidos(),
                p.getUsuario().getCorreo(),
                p.getDetalles().stream().map(DetallePedidoResponse::from).toList());
    }
}
