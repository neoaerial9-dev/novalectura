package com.novalectura.novalectura_backend.dto.request;

import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LibroRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 150) String autor,
        @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal precio,
        @NotNull @PositiveOrZero Integer stock,
        @Size(max = 2000) String descripcion,
        @Size(max = 500) String imagen,
        @NotNull CondicionLibro condicion,
        @NotNull Long idCategoria) {
}
