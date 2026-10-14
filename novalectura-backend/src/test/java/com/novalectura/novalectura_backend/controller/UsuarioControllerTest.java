package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.UsuarioService;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController (/api/users)")
class UsuarioControllerTest {
    @Mock UsuarioService usuarioService;
    private MockMvc mvc;

    private static UsuarioResponse usuario(Long id) {
        return new UsuarioResponse(id, "Ana", "Quispe", "ana@mail.com", Rol.USUARIO, null, null, null);
    }

    @BeforeEach
    void iniciar() {
        mvc = construir(new UsuarioController(usuarioService));
        autenticarComo(Datos.usuario(5L));
    }

    @AfterEach
    void limpiar() {
        cerrarSesion();
    }

    @Test
    @DisplayName("GET /me devuelve el perfil propio (y /me no se confunde con /{id})")
    void miPerfil() throws Exception {
        when(usuarioService.miPerfil(any(UsuarioDetails.class))).thenReturn(usuario(5L));

        mvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(5))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    @DisplayName("PUT /me actualiza el perfil con los datos del cuerpo")
    void actualizarPerfil() throws Exception {
        when(usuarioService.actualizarPerfil(any(UsuarioDetails.class), any(UsuarioPerfilRequest.class)))
                .thenReturn(usuario(5L));

        mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Ana\",\"apellidos\":\"Quispe\",\"telefono\":\"999\",\"direccion\":\"Calle 1\"}"))
                .andExpect(status().isOk());

        ArgumentCaptor<UsuarioPerfilRequest> captor = ArgumentCaptor.forClass(UsuarioPerfilRequest.class);
        verify(usuarioService).actualizarPerfil(any(UsuarioDetails.class), captor.capture());
        assertThat(captor.getValue().telefono()).isEqualTo("999");
        assertThat(captor.getValue().direccion()).isEqualTo("Calle 1");
    }

    @Test
    @DisplayName("PUT /me con nombres vacíos: 400 y no llega al servicio")
    void actualizarPerfil_invalido() throws Exception {
        mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"\",\"apellidos\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.apellidos").exists());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("PUT /me ignora intentos de cambiar el rol o el correo en el cuerpo")
    void actualizarPerfil_ignoraCamposSensibles() throws Exception {
        when(usuarioService.actualizarPerfil(any(UsuarioDetails.class), any(UsuarioPerfilRequest.class)))
                .thenReturn(usuario(5L));

        mvc.perform(put("/api/users/me").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombres\":\"Ana\",\"apellidos\":\"Quispe\",\"rol\":\"ADMIN\",\"correo\":\"otro@mail.com\"}"))
                .andExpect(status().isOk());

        assertThat(UsuarioPerfilRequest.class.getRecordComponents())
                .extracting(c -> c.getName())
                .containsExactly("nombres", "apellidos", "telefono", "direccion");
    }

    @Test
    @DisplayName("GET /api/users lista usuarios con paginación (RF-13)")
    void listar() throws Exception {
        when(usuarioService.listar(0, 12)).thenReturn(new PageResponse<>(List.of(usuario(1L), usuario(2L)), 0, 12, 2, 1));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido.length()").value(2))
                .andExpect(jsonPath("$.totalElementos").value(2));
    }

    @Test
    @DisplayName("GET /api/users/{id} devuelve un usuario; si no existe, 404")
    void obtener() throws Exception {
        when(usuarioService.obtener(7L)).thenReturn(usuario(7L));
        when(usuarioService.obtener(99L)).thenThrow(ApiException.noEncontrado("no existe"));

        mvc.perform(get("/api/users/7")).andExpect(status().isOk()).andExpect(jsonPath("$.idUsuario").value(7));
        mvc.perform(get("/api/users/99")).andExpect(status().isNotFound());
    }
}
