package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CambioContrasenaRequest;
import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.mapper.UsuarioMapper;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.novalectura.novalectura_backend.testutil.ErroresApi.assertApi;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService")
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;
    @Mock UsuarioMapper mapper;
    @InjectMocks UsuarioService servicio;

    private Usuario usuario;
    private UsuarioDetails actual;

    @BeforeEach
    void iniciar() {
        usuario = Datos.usuario(5L);
        actual = new UsuarioDetails(usuario);
    }

    private static UsuarioResponse respuesta() {
        return new UsuarioResponse(5L, "N", "A", "user5@mail.com", Rol.USUARIO, null, null, null);
    }

    @Test
    @DisplayName("miPerfil devuelve los datos del usuario autenticado")
    void miPerfil() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(respuesta());

        assertThat(servicio.miPerfil(actual).idUsuario()).isEqualTo(5L);
    }

    @Test
    @DisplayName("actualizarPerfil recorta los textos y guarda null en teléfono/dirección vacíos")
    void actualizarPerfil_limpiaDatos() {
        UsuarioPerfilRequest request = new UsuarioPerfilRequest("  Ana ", " Quispe  ", "   ", "");
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(respuesta());

        servicio.actualizarPerfil(actual, request);

        verify(mapper).actualizarPerfil(request, usuario);
        assertThat(usuario.getNombres()).isEqualTo("Ana");
        assertThat(usuario.getApellidos()).isEqualTo("Quispe");
        assertThat(usuario.getTelefono()).isNull();
        assertThat(usuario.getDireccion()).isNull();
    }

    @Test
    @DisplayName("actualizarPerfil conserva teléfono y dirección con valor (recortados)")
    void actualizarPerfil_conservaValores() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(respuesta());

        servicio.actualizarPerfil(actual, new UsuarioPerfilRequest("Ana", "Quispe", " 999888777 ", " Calle 1 "));

        assertThat(usuario.getTelefono()).isEqualTo("999888777");
        assertThat(usuario.getDireccion()).isEqualTo("Calle 1");
    }

    @Test
    @DisplayName("actualizarPerfil NUNCA cambia correo, rol ni contraseña")
    void actualizarPerfil_noTocaCamposSensibles() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(respuesta());

        servicio.actualizarPerfil(actual, new UsuarioPerfilRequest("Ana", "Quispe", null, null));

        assertThat(usuario.getCorreo()).isEqualTo("user5@mail.com");
        assertThat(usuario.getRol()).isEqualTo(Rol.USUARIO);
        assertThat(usuario.getContrasena()).isEqualTo("hash-5");
    }

    @Test
    @DisplayName("cambiar contraseña con la actual incorrecta: 400 y no se modifica nada")
    void cambiarContrasena_actualIncorrecta() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(encoder.matches("equivocada", "hash-5")).thenReturn(false);

        assertApi(HttpStatus.BAD_REQUEST,
                () -> servicio.cambiarContrasena(actual, new CambioContrasenaRequest("equivocada", "nueva-clave-1")));

        assertThat(usuario.getContrasena()).isEqualTo("hash-5");
        verify(encoder, never()).encode(any());
    }

    @Test
    @DisplayName("la nueva contraseña debe ser distinta de la actual")
    void cambiarContrasena_igual() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(encoder.matches("misma-clave-1", "hash-5")).thenReturn(true);

        assertApi(HttpStatus.BAD_REQUEST,
                () -> servicio.cambiarContrasena(actual, new CambioContrasenaRequest("misma-clave-1", "misma-clave-1")));

        assertThat(usuario.getContrasena()).isEqualTo("hash-5");
    }

    @Test
    @DisplayName("con la actual correcta, la nueva se guarda cifrada")
    void cambiarContrasena_ok() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(encoder.matches("vieja-clave-1", "hash-5")).thenReturn(true);
        when(encoder.encode("nueva-clave-1")).thenReturn("HASH-NUEVO");

        servicio.cambiarContrasena(actual, new CambioContrasenaRequest("vieja-clave-1", "nueva-clave-1"));

        assertThat(usuario.getContrasena()).isEqualTo("HASH-NUEVO");
    }

    @Test
    @DisplayName("listar usuarios pagina, acota el tamaño a 50 y ordena por registro descendente")
    void listar() {
        when(usuarios.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        PageResponse<UsuarioResponse> resultado = servicio.listar(-1, 999);

        verify(usuarios).findAll(captor.capture());
        assertThat(resultado.contenido()).isEmpty();
        assertThat(captor.getValue().getPageNumber()).isZero();
        assertThat(captor.getValue().getPageSize()).isEqualTo(50);
        Sort.Order orden = captor.getValue().getSort().getOrderFor("fechaRegistro");
        assertThat(orden).isNotNull();
        assertThat(orden.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("consultar un usuario por id")
    void obtener_ok() {
        when(usuarios.findById(5L)).thenReturn(Optional.of(usuario));
        when(mapper.toResponse(usuario)).thenReturn(respuesta());

        assertThat(servicio.obtener(5L).correo()).isEqualTo("user5@mail.com");
    }

    @Test
    @DisplayName("consultar un usuario inexistente responde 404")
    void obtener_noExiste() {
        when(usuarios.findById(99L)).thenReturn(Optional.empty());

        assertApi(HttpStatus.NOT_FOUND, () -> servicio.obtener(99L));
    }
}
