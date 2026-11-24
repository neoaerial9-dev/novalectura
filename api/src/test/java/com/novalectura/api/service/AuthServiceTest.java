package com.novalectura.api.service;

import com.novalectura.api.dto.AuthResponse;
import com.novalectura.api.dto.LoginRequest;
import com.novalectura.api.dto.RegistroRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.UsuarioRepository;
import com.novalectura.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock ApplicationEventPublisher eventos;
    @InjectMocks AuthService authService;

    @Test
    void registrar_creaClienteConContrasenaCodificadaYCorreoNormalizado() {
        when(usuarioRepository.existsByCorreo("maria@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        UsuarioResponse respuesta = authService.registrar(
                new RegistroRequest("María", "García", "  Maria@Mail.com ", "secreto123"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();
        assertThat(guardado.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(guardado.getContrasena()).isEqualTo("HASH");
        assertThat(guardado.getCorreo()).isEqualTo("maria@mail.com");
        assertThat(guardado.isActivo()).isTrue();
        assertThat(respuesta.correo()).isEqualTo("maria@mail.com");
    }

    @Test
    void registrar_avisaQueCambioLaListaDeUsuarios() {
        when(usuarioRepository.existsByCorreo("maria@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("secreto123")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        authService.registrar(new RegistroRequest("María", "García", "maria@mail.com", "secreto123"));

        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.USUARIOS));
    }

    @Test
    void registrar_correoYaRegistrado_lanzaConflicto() {
        when(usuarioRepository.existsByCorreo("maria@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(
                new RegistroRequest("María", "García", "maria@mail.com", "secreto123")))
                .isInstanceOf(ConflictoException.class);

        verify(usuarioRepository, never()).save(any());
        verifyNoInteractions(eventos);
    }

    @Test
    void login_credencialesValidas_devuelveTokenYUsuario() {
        Usuario usuario = Usuario.builder().id(1L).nombres("Ana").apellidos("Paz")
                .correo("ana@mail.com").contrasena("HASH").rol(Rol.CLIENTE).build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("ana@mail.com", "x"));
        when(usuarioRepository.findByCorreo("ana@mail.com")).thenReturn(Optional.of(usuario));
        when(jwtService.generarToken("ana@mail.com", Rol.CLIENTE)).thenReturn("TOKEN");

        AuthResponse respuesta = authService.login(new LoginRequest(" Ana@Mail.com", "secreto123"));

        assertThat(respuesta.token()).isEqualTo("TOKEN");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.usuario().rol()).isEqualTo(Rol.CLIENTE);
    }

    @Test
    void login_credencialesInvalidas_propagaLaExcepcionSinEmitirToken() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@mail.com", "incorrecta")))
                .isInstanceOf(BadCredentialsException.class);

        verifyNoInteractions(jwtService);
    }
}
