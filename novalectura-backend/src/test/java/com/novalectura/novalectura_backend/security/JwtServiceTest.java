package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService")
class JwtServiceTest {
    private JwtService servicio;
    private Usuario usuario;

    @BeforeEach
    void iniciar() {
        servicio = new JwtService(new JwtProperties(secretoAleatorio(32), 60));
        usuario = Datos.usuario(1L, Rol.ADMIN);
    }

    private static String secretoAleatorio(int bytes) {
        byte[] datos = new byte[bytes];
        new SecureRandom().nextBytes(datos);
        return Base64.getEncoder().encodeToString(datos);
    }

    private static String payload(String token) {
        return new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("sin clave configurada la aplicación no arranca y explica cómo arreglarlo")
    void sinClave() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(null, 60)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService(new JwtProperties("   ", 60)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    @DisplayName("una clave de menos de 32 bytes se rechaza")
    void claveCorta() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(secretoAleatorio(16), 60)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    @DisplayName("un token generado devuelve el correo del usuario al leerlo")
    void ida_y_vuelta() {
        String token = servicio.generarToken(usuario);
        assertThat(servicio.extraerCorreo(token)).contains("user1@mail.com");
    }

    @Test
    @DisplayName("el token lleva el rol, pero nunca la contraseña del usuario")
    void contenidoDelToken() {
        String contenido = payload(servicio.generarToken(usuario));

        assertThat(contenido).contains("\"sub\":\"user1@mail.com\"");
        assertThat(contenido).contains("\"rol\":\"ADMIN\"");
        assertThat(contenido).doesNotContain(usuario.getContrasena());
    }

    @Test
    @DisplayName("un token expirado se rechaza")
    void tokenExpirado() {
        JwtService expirado = new JwtService(new JwtProperties(secretoAleatorio(32), -1));
        assertThat(expirado.extraerCorreo(expirado.generarToken(usuario))).isEmpty();
    }

    @Test
    @DisplayName("un token firmado con otra clave se rechaza")
    void otraClave() {
        JwtService otro = new JwtService(new JwtProperties(secretoAleatorio(32), 60));
        assertThat(servicio.extraerCorreo(otro.generarToken(usuario))).isEmpty();
    }

    @Test
    @DisplayName("un token con el contenido alterado (otro usuario) se rechaza")
    void tokenManipulado() {
        String[] a = servicio.generarToken(Datos.usuario(1L)).split("\\.");
        String[] b = servicio.generarToken(Datos.usuario(2L, Rol.ADMIN)).split("\\.");

        String manipulado = a[0] + "." + b[1] + "." + a[2];

        assertThat(servicio.extraerCorreo(manipulado)).isEmpty();
    }

    @Test
    @DisplayName("texto que no es un token se rechaza sin lanzar excepción")
    void basura() {
        assertThat(servicio.extraerCorreo("esto-no-es-un-jwt")).isEmpty();
        assertThat(servicio.extraerCorreo("")).isEmpty();
        assertThat(servicio.extraerCorreo("a.b.c")).isEmpty();
    }
}
