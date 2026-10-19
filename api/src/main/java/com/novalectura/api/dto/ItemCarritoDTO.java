package com.novalectura.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ItemCarritoDTO {
    private Long idLibro;
    private String tituloLibro;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}
