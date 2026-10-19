package com.novalectura.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PedidoResponseDTO {
    private Long idPedido;
    private LocalDateTime fechaPedido;
    private Double total;
    private String estado;
    private String nombreCliente;
    private List<ItemCarritoDTO> detalles;
}
