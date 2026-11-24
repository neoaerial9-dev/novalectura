package com.novalectura.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemCarritoRequest(
        @NotNull(message = "El libro es obligatorio") Long libroId,
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad) {
}
