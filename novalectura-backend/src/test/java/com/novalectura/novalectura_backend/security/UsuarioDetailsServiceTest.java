package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioDetailsService")
class UsuarioDetailsServiceTest {
    @Mock UsuarioRepository usuarios;
    @InjectMocks UsuarioDetailsService servicio;

    @Test
    @DisplayName("busca por correo normalizado (sin espacios y en minúsculas)")
    void normalizaCorreo() {
        when(usuarios.findByCorreo("user1@mail.com")).thenReturn(Optional.of(Datos.usuario(1L)));

        UserDetails detalles = servicio.loadUserByUsername("  USER1@Mail.com ");

        assertThat(detalles.getUsername()).isEqualTo("user1@mail.com");
        verify(usuarios).findByCorreo("user1@mail.com");
    }

    @Test
    @DisplayName("un correo que no existe lanza UsernameNotFoundException")
    void noExiste() {
        when(usuarios.findByCorreo("nadie@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.loadUserByUsername("nadie@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
