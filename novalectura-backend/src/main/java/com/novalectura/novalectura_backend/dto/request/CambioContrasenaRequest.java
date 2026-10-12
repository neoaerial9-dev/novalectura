package com.novalectura.novalectura_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioContrasenaRequest(
        @NotBlank String contrasenaActual,

        @NotBlank @Size(min = 8, max = 72) String contrasenaNueva) {
}
