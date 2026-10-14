package com.novalectura.novalectura_backend.config;

import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminInitializer")
class AdminInitializerTest {
    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;

    private AdminInitializer inicializador(String correo, String contrasena) {
        return new AdminInitializer(usuarios, encoder, correo, contrasena);
    }

    @Test
    @DisplayName("crea el administrador con correo normalizado, rol ADMIN y contraseña cifrada")
    void creaAdmin() {
        when(usuarios.existsByCorreo("admin@mail.com")).thenReturn(false);
        when(encoder.encode("secreta123")).thenReturn("HASH");

        inicializador("  ADMIN@Mail.com ", "secreta123").run(null);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario admin = captor.getValue();
        assertThat(admin.getCorreo()).isEqualTo("admin@mail.com");
        assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
        assertThat(admin.getContrasena()).isEqualTo("HASH");
    }

    @Test
    @DisplayName("si el administrador ya existe no lo duplica ni lo modifica")
    void yaExiste() {
        when(usuarios.existsByCorreo("admin@mail.com")).thenReturn(true);

        inicializador("admin@mail.com", "secreta123").run(null);

        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("sin variables de entorno (correo o contraseña vacíos) no crea nada")
    void sinConfiguracion() {
        inicializador("", "").run(null);
        inicializador("admin@mail.com", "").run(null);
        inicializador("", "secreta123").run(null);

        verify(usuarios, never()).save(any(Usuario.class));
    }
}
