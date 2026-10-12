package com.novalectura.novalectura_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CarritoCantidadRequest(
        @NotNull @Min(1) Integer cantidad) {
}
