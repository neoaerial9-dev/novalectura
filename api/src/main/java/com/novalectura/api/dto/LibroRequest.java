package com.novalectura.api.dto;

import com.novalectura.api.model.CondicionLibro;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LibroRequest(
        @NotBlank(message = "El título es obligatorio") @Size(max = 150) String titulo,
        @NotBlank(message = "El autor es obligatorio") @Size(max = 120) String autor,
        @Size(max = 100) String editorial,
        @Size(max = 20) String isbn,
        @Min(value = 1000, message = "El año de publicación no es válido")
        @Max(value = 2100, message = "El año de publicación no es válido") Integer anioPublicacion,
        @Size(max = 2000) String descripcion,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo") Integer stock,
        @Size(max = 500)
        @Pattern(regexp = "^(https?://\\S+|/api/imagenes/[A-Za-z0-9._-]+)$",
                message = "La imagen debe ser una portada subida o una URL http(s)") String imagen,
        @NotNull(message = "La condición del libro es obligatoria") CondicionLibro condicion,
        @NotNull(message = "La categoría es obligatoria") Long categoriaId) {
}
