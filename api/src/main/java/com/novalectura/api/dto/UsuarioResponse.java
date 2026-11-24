package com.novalectura.api.dto;

import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nombres, String apellidos, String correo, Rol rol,
                              String telefono, String direccionEnvio, boolean activo,
                              LocalDateTime fechaRegistro) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombres(), u.getApellidos(), u.getCorreo(), u.getRol(),
                u.getTelefono(), u.getDireccionEnvio(), u.isActivo(), u.getFechaRegistro());
    }
}
