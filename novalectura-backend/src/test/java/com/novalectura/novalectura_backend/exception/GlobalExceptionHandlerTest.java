package com.novalectura.novalectura_backend.exception;

import com.novalectura.novalectura_backend.entity.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler manejador = new GlobalExceptionHandler();

    @SuppressWarnings("unused")
    private void metodoFalso(String entrada) {
    }

    @Test
    @DisplayName("ApiException se traduce a su estado HTTP y su mensaje")
    void apiException() {
        ProblemDetail problema = manejador.manejarApi(ApiException.conflicto("ya existe"));

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getDetail()).isEqualTo("ya existe");
    }

    @Test
    @DisplayName("errores de validación: 400 con el detalle por campo (se conserva el primer mensaje de cada uno)")
    void validacion() throws Exception {
        MethodParameter parametro = new MethodParameter(
                getClass().getDeclaredMethod("metodoFalso", String.class), 0);
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(new Object(), "request");
        resultado.addError(new FieldError("request", "correo", "primer error"));
        resultado.addError(new FieldError("request", "correo", "segundo error"));
        resultado.addError(new FieldError("request", "contrasena", "muy corta"));

        ProblemDetail problema = manejador.manejarValidacion(new MethodArgumentNotValidException(parametro, resultado));

        assertThat(problema.getStatus()).isEqualTo(400);
        assertThat(problema.getProperties()).containsKey("errores");
        Map<?, ?> errores = (Map<?, ?>) problema.getProperties().get("errores");
        assertThat(errores).hasSize(2);
        assertThat(errores.get("correo")).isEqualTo("primer error");
        assertThat(errores.get("contrasena")).isEqualTo("muy corta");
    }

    @Test
    @DisplayName("credenciales incorrectas: 401 con mensaje genérico (no revela si el correo existe)")
    void credenciales() {
        ProblemDetail problema = manejador.manejarCredenciales(new BadCredentialsException("cualquier cosa interna"));

        assertThat(problema.getStatus()).isEqualTo(401);
        assertThat(problema.getDetail()).doesNotContain("cualquier cosa interna");
    }

    @Test
    @DisplayName("bloqueo optimista (dos compradores, mismo ejemplar): 409")
    void bloqueoOptimista() {
        ProblemDetail problema = manejador.manejarBloqueoOptimista(
                new ObjectOptimisticLockingFailureException(Libro.class, 1L));

        assertThat(problema.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("violación de integridad: 409 sin filtrar el detalle interno de la base de datos")
    void integridad() {
        ProblemDetail problema = manejador.manejarIntegridad(
                new DataIntegrityViolationException("Duplicate entry 'x' for key 'usuarios.UK_correo'"));

        assertThat(problema.getStatus()).isEqualTo(409);
        assertThat(problema.getDetail()).doesNotContain("Duplicate entry").doesNotContain("UK_correo");
    }
}
