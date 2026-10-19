package com.novalectura.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UsuarioResponseDTO {
    private Long idUsuario;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
}
