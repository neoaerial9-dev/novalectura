package com.novalectura.novalectura_backend.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig")
class SecurityConfigTest {
    private final SecurityConfig configuracion = new SecurityConfig();

    @Test
    @DisplayName("las contraseñas se guardan con BCrypt: el hash no es el texto y se puede verificar")
    void passwordEncoder() {
        PasswordEncoder encoder = configuracion.passwordEncoder();

        String hash = encoder.encode("clave12345");

        assertThat(hash).isNotEqualTo("clave12345").startsWith("$2");
        assertThat(encoder.matches("clave12345", hash)).isTrue();
        assertThat(encoder.matches("otra-clave", hash)).isFalse();
    }

    @Test
    @DisplayName("el mismo texto produce hashes distintos (sal aleatoria)")
    void passwordEncoder_sal() {
        PasswordEncoder encoder = configuracion.passwordEncoder();
        assertThat(encoder.encode("clave12345")).isNotEqualTo(encoder.encode("clave12345"));
    }

    @Test
    @DisplayName("CORS permite solo los orígenes configurados, en rutas /api")
    void cors() {
        CorsConfigurationSource fuente = configuracion.corsConfigurationSource(List.of("http://localhost:4200"));

        MockHttpServletRequest api = new MockHttpServletRequest("GET", "/api/books");
        CorsConfiguration cors = fuente.getCorsConfiguration(api);

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(cors.getAllowedMethods()).contains("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).contains("Authorization", "Content-Type");
        assertThat(cors.getAllowedOrigins()).doesNotContain("*");
    }

    @Test
    @DisplayName("fuera de /api no se aplica CORS")
    void cors_fueraDeApi() {
        CorsConfigurationSource fuente = configuracion.corsConfigurationSource(List.of("http://localhost:4200"));
        assertThat(fuente.getCorsConfiguration(new MockHttpServletRequest("GET", "/otra-ruta"))).isNull();
    }
}
