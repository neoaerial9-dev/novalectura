package com.novalectura.api.security;

import com.novalectura.api.model.Rol;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRETO = "clave-de-pruebas-con-mas-de-treinta-y-dos-caracteres";
    private final JwtService jwtService = new JwtService(SECRETO, 60_000);

    @Test
    void generaTokenValidoConCorreoYRol() {
        String token = jwtService.generarToken("ana@mail.com", Rol.CLIENTE);

        assertThat(jwtService.esTokenValido(token)).isTrue();
        assertThat(jwtService.extraerCorreo(token)).isEqualTo("ana@mail.com");
        assertThat(jwtService.extraerRol(token)).isEqualTo("CLIENTE");
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService expirado = new JwtService(SECRETO, -1_000);

        assertThat(jwtService.esTokenValido(expirado.generarToken("ana@mail.com", Rol.CLIENTE))).isFalse();
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        JwtService otro = new JwtService("otra-clave-distinta-con-mas-de-treinta-y-dos-caracteres", 60_000);

        assertThat(jwtService.esTokenValido(otro.generarToken("ana@mail.com", Rol.ADMINISTRADOR))).isFalse();
    }

    @Test
    void rechazaTokenManipulado() {
        String token = jwtService.generarToken("ana@mail.com", Rol.CLIENTE);
        String[] partes = token.split("\\.");
        String payloadAlterado = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"admin@mail.com\",\"rol\":\"ADMINISTRADOR\"}"
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThat(jwtService.esTokenValido(partes[0] + "." + payloadAlterado + "." + partes[2])).isFalse();
    }

    @Test
    void rechazaTextoQueNoEsUnToken() {
        assertThat(jwtService.esTokenValido("no-es-un-token")).isFalse();
        assertThat(jwtService.esTokenValido("")).isFalse();
    }
}
