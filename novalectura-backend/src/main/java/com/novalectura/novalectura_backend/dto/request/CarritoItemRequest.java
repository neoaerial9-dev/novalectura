package com.novalectura.novalectura_backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CarritoItemRequest(
        @NotNull Long idLibro,
        @NotNull @Min(1) Integer cantidad) {
}
