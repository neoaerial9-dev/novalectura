package com.novalectura.novalectura_backend.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiException")
class ApiExceptionTest {
    @Test
    @DisplayName("cada fábrica produce el estado HTTP que corresponde y conserva el mensaje")
    void fabricas() {
        assertThat(ApiException.solicitudInvalida("a").getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ApiException.prohibido("b").getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ApiException.noEncontrado("c").getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ApiException.conflicto("d").getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ApiException.conflicto("mensaje").getMessage()).isEqualTo("mensaje");
    }
}
