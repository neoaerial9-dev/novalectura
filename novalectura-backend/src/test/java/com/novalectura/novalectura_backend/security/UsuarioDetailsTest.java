package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UsuarioDetails")
class UsuarioDetailsTest {
    @Test
    @DisplayName("expone correo como username y el hash como password")
    void credenciales() {
        UsuarioDetails detalles = new UsuarioDetails(Datos.usuario(7L));

        assertThat(detalles.getUsername()).isEqualTo("user7@mail.com");
        assertThat(detalles.getPassword()).isEqualTo("hash-7");
        assertThat(detalles.getIdUsuario()).isEqualTo(7L);
    }

    @Test
    @DisplayName("el rol se traduce a la autoridad ROLE_<rol> que usa hasRole()")
    void autoridades() {
        assertThat(new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN)).getAuthorities())
                .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ADMIN");
        assertThat(new UsuarioDetails(Datos.usuario(1L, Rol.USUARIO)).getAuthorities())
                .extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USUARIO");
    }

    @Test
    @DisplayName("esAdmin solo es verdadero para el rol ADMIN")
    void esAdmin() {
        assertThat(new UsuarioDetails(Datos.usuario(1L, Rol.ADMIN)).esAdmin()).isTrue();
        assertThat(new UsuarioDetails(Datos.usuario(1L, Rol.USUARIO)).esAdmin()).isFalse();
    }

    @Test
    @DisplayName("la cuenta no está bloqueada, expirada ni deshabilitada")
    void estadoCuenta() {
        UsuarioDetails detalles = new UsuarioDetails(Datos.usuario(1L));
        assertThat(detalles.isEnabled()).isTrue();
        assertThat(detalles.isAccountNonLocked()).isTrue();
        assertThat(detalles.isAccountNonExpired()).isTrue();
        assertThat(detalles.isCredentialsNonExpired()).isTrue();
    }
}
