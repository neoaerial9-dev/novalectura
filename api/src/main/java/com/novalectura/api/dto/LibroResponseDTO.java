package com.novalectura.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LibroResponseDTO {
    private Long idLibro;
    private String titulo;
    private String autor;
    private Double precio;
    private Integer stock;
    private String descripcion;
    private String imagen;
    private String estadoConservacion;
    private String detallesUso;
    private String nombreCategoria;
}
