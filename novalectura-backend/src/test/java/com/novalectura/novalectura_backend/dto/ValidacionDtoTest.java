package com.novalectura.novalectura_backend.dto;

import com.novalectura.novalectura_backend.dto.request.*;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación de DTOs de entrada")
class ValidacionDtoTest {
    private static final Validator VALIDADOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static Set<String> camposInvalidos(Object dto) {
        return VALIDADOR.validate(dto).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("RegistroRequest válido no tiene errores")
    void registro_valido() {
        assertThat(camposInvalidos(new RegistroRequest("Ana", "Quispe", "ana@mail.com", "clave12345"))).isEmpty();
    }

    @Test
    @DisplayName("RegistroRequest: correo mal formado, nombres vacíos y contraseña corta")
    void registro_invalido() {
        assertThat(camposInvalidos(new RegistroRequest("Ana", "Quispe", "no-es-correo", "clave12345")))
                .containsExactly("correo");
        assertThat(camposInvalidos(new RegistroRequest(" ", "", "ana@mail.com", "clave12345")))
                .containsExactlyInAnyOrder("nombres", "apellidos");
        assertThat(camposInvalidos(new RegistroRequest("Ana", "Quispe", "ana@mail.com", "corta")))
                .containsExactly("contrasena");
    }

    @Test
    @DisplayName("RegistroRequest: la contraseña tiene un máximo de 72 (límite de BCrypt)")
    void registro_contrasenaLimite() {
        assertThat(camposInvalidos(new RegistroRequest("Ana", "Quispe", "ana@mail.com", "a".repeat(72)))).isEmpty();
        assertThat(camposInvalidos(new RegistroRequest("Ana", "Quispe", "ana@mail.com", "a".repeat(73))))
                .containsExactly("contrasena");
    }

    @Test
    @DisplayName("LoginRequest exige correo válido y contraseña")
    void login() {
        assertThat(camposInvalidos(new LoginRequest("ana@mail.com", "x"))).isEmpty();
        assertThat(camposInvalidos(new LoginRequest("", ""))).containsExactlyInAnyOrder("correo", "contrasena");
        assertThat(camposInvalidos(new LoginRequest("no-correo", "x"))).containsExactly("correo");
    }

    private static LibroRequest libro(String titulo, BigDecimal precio, Integer stock,
                                      CondicionLibro condicion, Long idCategoria) {
        return new LibroRequest(titulo, "Autor", precio, stock, "desc", "img.jpg", condicion, idCategoria);
    }

    @Test
    @DisplayName("LibroRequest válido no tiene errores (stock 0 es aceptado)")
    void libro_valido() {
        assertThat(camposInvalidos(libro("Dune", new BigDecimal("24.99"), 1, CondicionLibro.BUENO, 1L))).isEmpty();
        assertThat(camposInvalidos(libro("Dune", new BigDecimal("24.99"), 0, CondicionLibro.BUENO, 1L))).isEmpty();
    }

    @Test
    @DisplayName("LibroRequest: precio 0, negativo, nulo o con más de 2 decimales es inválido")
    void libro_precio() {
        assertThat(camposInvalidos(libro("Dune", BigDecimal.ZERO, 1, CondicionLibro.BUENO, 1L))).containsExactly("precio");
        assertThat(camposInvalidos(libro("Dune", new BigDecimal("-5"), 1, CondicionLibro.BUENO, 1L))).containsExactly("precio");
        assertThat(camposInvalidos(libro("Dune", null, 1, CondicionLibro.BUENO, 1L))).containsExactly("precio");
        assertThat(camposInvalidos(libro("Dune", new BigDecimal("10.999"), 1, CondicionLibro.BUENO, 1L))).containsExactly("precio");
    }

    @Test
    @DisplayName("LibroRequest: stock negativo, título vacío o demasiado largo, sin condición ni categoría")
    void libro_otrosCampos() {
        assertThat(camposInvalidos(libro("Dune", BigDecimal.ONE, -1, CondicionLibro.BUENO, 1L))).containsExactly("stock");
        assertThat(camposInvalidos(libro("  ", BigDecimal.ONE, 1, CondicionLibro.BUENO, 1L))).containsExactly("titulo");
        assertThat(camposInvalidos(libro("t".repeat(201), BigDecimal.ONE, 1, CondicionLibro.BUENO, 1L))).containsExactly("titulo");
        assertThat(camposInvalidos(libro("Dune", BigDecimal.ONE, 1, null, 1L))).containsExactly("condicion");
        assertThat(camposInvalidos(libro("Dune", BigDecimal.ONE, 1, CondicionLibro.BUENO, null))).containsExactly("idCategoria");
    }

    @Test
    @DisplayName("CarritoItemRequest: libro obligatorio y cantidad mínima 1")
    void carritoItem() {
        assertThat(camposInvalidos(new CarritoItemRequest(10L, 1))).isEmpty();
        assertThat(camposInvalidos(new CarritoItemRequest(null, 1))).containsExactly("idLibro");
        assertThat(camposInvalidos(new CarritoItemRequest(10L, 0))).containsExactly("cantidad");
        assertThat(camposInvalidos(new CarritoItemRequest(10L, null))).containsExactly("cantidad");
    }

    @Test
    @DisplayName("CarritoCantidadRequest: cantidad mínima 1")
    void carritoCantidad() {
        assertThat(camposInvalidos(new CarritoCantidadRequest(3))).isEmpty();
        assertThat(camposInvalidos(new CarritoCantidadRequest(0))).containsExactly("cantidad");
        assertThat(camposInvalidos(new CarritoCantidadRequest(-2))).containsExactly("cantidad");
    }

    @Test
    @DisplayName("CategoriaRequest: nombre obligatorio y de hasta 100 caracteres")
    void categoria() {
        assertThat(camposInvalidos(new CategoriaRequest("Fantasía", null))).isEmpty();
        assertThat(camposInvalidos(new CategoriaRequest(" ", null))).containsExactly("nombre");
        assertThat(camposInvalidos(new CategoriaRequest("n".repeat(101), null))).containsExactly("nombre");
    }

    @Test
    @DisplayName("UsuarioPerfilRequest: teléfono y dirección son opcionales pero acotados")
    void perfil() {
        assertThat(camposInvalidos(new UsuarioPerfilRequest("Ana", "Quispe", null, null))).isEmpty();
        assertThat(camposInvalidos(new UsuarioPerfilRequest("Ana", "Quispe", "1".repeat(21), null)))
                .containsExactly("telefono");
        assertThat(camposInvalidos(new UsuarioPerfilRequest("Ana", "Quispe", null, "d".repeat(256))))
                .containsExactly("direccion");
        assertThat(camposInvalidos(new UsuarioPerfilRequest("", "Quispe", null, null))).containsExactly("nombres");
    }

    @Test
    @DisplayName("CambioContrasenaRequest: la nueva debe tener entre 8 y 72 caracteres")
    void cambioContrasena() {
        assertThat(camposInvalidos(new CambioContrasenaRequest("actual", "nueva-clave-1"))).isEmpty();
        assertThat(camposInvalidos(new CambioContrasenaRequest("", "nueva-clave-1"))).containsExactly("contrasenaActual");
        assertThat(camposInvalidos(new CambioContrasenaRequest("actual", "corta"))).containsExactly("contrasenaNueva");
        assertThat(camposInvalidos(new CambioContrasenaRequest("actual", "n".repeat(73)))).containsExactly("contrasenaNueva");
    }
}
