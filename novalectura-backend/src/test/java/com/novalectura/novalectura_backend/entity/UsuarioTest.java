package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.entity.enums.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario")
class UsuarioTest {
    @Test
    @DisplayName("un usuario nuevo es USUARIO por defecto, nunca ADMIN")
    void rolPorDefecto() {
        assertThat(new Usuario().getRol()).isEqualTo(Rol.USUARIO);
    }

    @Test
    @DisplayName("al persistir se registra la fecha de registro")
    void prePersist_asignaFecha() {
        Usuario usuario = new Usuario();
        assertThat(usuario.getFechaRegistro()).isNull();
        usuario.alCrear();
        assertThat(usuario.getFechaRegistro()).isNotNull();
    }
}
