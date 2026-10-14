package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.LoginRequest;
import com.novalectura.novalectura_backend.dto.request.RegistroRequest;
import com.novalectura.novalectura_backend.dto.response.AuthResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.mapper.UsuarioMapper;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.JwtService;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock UsuarioMapper usuarioMapper;
    @InjectMocks AuthService servicio;

    private static UsuarioResponse respuestaUsuario() {
        return new UsuarioResponse(1L, "Ana", "Quispe", "ana@mail.com", Rol.USUARIO, null, null, null);
    }

    @Test
    @DisplayName("registra al usuario con datos limpios, correo en minúsculas, rol USUARIO y contraseña cifrada")
    void registrar_ok() {
        when(usuarios.existsByCorreo("ana@mail.com")).thenReturn(false);
        when(encoder.encode("clave12345")).thenReturn("HASH");
        when(jwtService.generarToken(any(Usuario.class))).thenReturn("TOKEN");
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(respuestaUsuario());

        AuthResponse respuesta = servicio.registrar(
                new RegistroRequest("  Ana ", " Quispe ", "  ANA@Mail.com ", "clave12345"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getNombres()).isEqualTo("Ana");
        assertThat(guardado.getApellidos()).isEqualTo("Quispe");
        assertThat(guardado.getCorreo()).isEqualTo("ana@mail.com");
        assertThat(guardado.getRol()).isEqualTo(Rol.USUARIO);
        assertThat(guardado.getContrasena()).isEqualTo("HASH").isNotEqualTo("clave12345");

        assertThat(respuesta.token()).isEqualTo("TOKEN");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.usuario().correo()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("un correo ya registrado se rechaza con 409 y no se guarda nada")
    void registrar_correoDuplicado() {
        when(usuarios.existsByCorreo("ana@mail.com")).thenReturn(true);

        assertApi(HttpStatus.CONFLICT, () -> servicio.registrar(
                new RegistroRequest("Ana", "Quispe", "ANA@mail.com", "clave12345")));

        verify(usuarios, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("autentica con el correo normalizado y devuelve un token y el usuario")
    void iniciarSesion_ok() {
        Usuario usuario = Datos.usuario(1L);
        UsuarioDetails detalles = new UsuarioDetails(usuario);
        Authentication autenticada = UsernamePasswordAuthenticationToken.authenticated(
                detalles, null, detalles.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticada);
        when(jwtService.generarToken(usuario)).thenReturn("TOKEN");
        when(usuarioMapper.toResponse(usuario)).thenReturn(respuestaUsuario());

        AuthResponse respuesta = servicio.iniciarSesion(new LoginRequest("  ANA@Mail.com", "clave12345"));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("ana@mail.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("clave12345");
        assertThat(respuesta.token()).isEqualTo("TOKEN");
    }

    @Test
    @DisplayName("credenciales incorrectas: el error se propaga y no se genera ningún token")
    void iniciarSesion_credencialesIncorrectas() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("mal"));

        assertThatThrownBy(() -> servicio.iniciarSesion(new LoginRequest("ana@mail.com", "incorrecta")))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generarToken(any(Usuario.class));
    }
}
