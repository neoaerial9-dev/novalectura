package com.novalectura.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequest(
        @NotBlank(message = "La contraseña actual es obligatoria") String actual,
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres") String nueva) {
}
