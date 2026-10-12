package com.novalectura.novalectura_backend.dto.response;

import com.novalectura.novalectura_backend.entity.enums.Rol;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long idUsuario,
        String nombres,
        String apellidos,
        String correo,
        Rol rol,
        String telefono,
        String direccion,
        LocalDateTime fechaRegistro) {
}
