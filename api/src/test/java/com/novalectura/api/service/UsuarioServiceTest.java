package com.novalectura.api.service;

import com.novalectura.api.dto.CambioContrasenaRequest;
import com.novalectura.api.dto.PerfilRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String CORREO = "ana@mail.com";

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ApplicationEventPublisher eventos;
    @InjectMocks UsuarioService usuarioService;

    private Usuario ana() {
        return Usuario.builder().id(1L).nombres("Ana").apellidos("Paz").correo(CORREO)
                .contrasena("HASH").rol(Rol.CLIENTE).build();
    }

    @Test
    void listar_devuelveUnaPaginaDeUsuarios() {
        Pageable pagina = PageRequest.of(0, 10);
        when(usuarioRepository.findAll(pagina)).thenReturn(new PageImpl<>(List.of(ana()), pagina, 1));

        Page<UsuarioResponse> resultado = usuarioService.listar(pagina);

        assertThat(resultado.getContent()).extracting(UsuarioResponse::correo).containsExactly(CORREO);
    }

    @Test
    void obtenerPorId_existente_devuelveElUsuario() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana()));

        assertThat(usuarioService.obtenerPorId(1L).nombres()).isEqualTo("Ana");
    }

    @Test
    void obtenerPorId_inexistente_lanzaNoEncontrado() {
        when(usuarioRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.obtenerPorId(9L)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void obtenerPerfil_devuelveLosDatosDelUsuarioAutenticado() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(ana()));

        assertThat(usuarioService.obtenerPerfil(CORREO).correo()).isEqualTo(CORREO);
    }

    @Test
    void actualizarPerfil_modificaLosDatosPersonalesPeroNoElCorreoNiElRol() {
        Usuario usuario = ana();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        UsuarioResponse respuesta = usuarioService.actualizarPerfil(CORREO,
                new PerfilRequest("Ana María", "Paz Soto", "999111222", "Calle Mercaderes 123"));

        assertThat(respuesta.nombres()).isEqualTo("Ana María");
        assertThat(usuario.getApellidos()).isEqualTo("Paz Soto");
        assertThat(usuario.getTelefono()).isEqualTo("999111222");
        assertThat(usuario.getDireccionEnvio()).isEqualTo("Calle Mercaderes 123");
        assertThat(usuario.getCorreo()).isEqualTo(CORREO);
        assertThat(usuario.getRol()).isEqualTo(Rol.CLIENTE);
        verify(eventos).publishEvent(new CambioEvent(RecursoCambiado.USUARIOS));
    }

    @Test
    void cambiarContrasena_conLaActualCorrecta_guardaLaNuevaCodificada() {
        Usuario usuario = ana();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("vieja1234", "HASH")).thenReturn(true);
        when(passwordEncoder.encode("nueva1234")).thenReturn("HASH2");

        usuarioService.cambiarContrasena(CORREO, new CambioContrasenaRequest("vieja1234", "nueva1234"));

        assertThat(usuario.getContrasena()).isEqualTo("HASH2");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarContrasena_conLaActualIncorrecta_lanzaSolicitudInvalida() {
        Usuario usuario = ana();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cambiarContrasena(CORREO,
                new CambioContrasenaRequest("mala", "nueva1234")))
                .isInstanceOf(SolicitudInvalidaException.class);

        assertThat(usuario.getContrasena()).isEqualTo("HASH");
        verify(usuarioRepository, never()).save(any());
    }
}
