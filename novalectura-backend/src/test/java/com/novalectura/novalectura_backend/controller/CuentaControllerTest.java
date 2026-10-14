package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.CambioContrasenaRequest;
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

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuentaController (PUT /api/auth/password)")
class CuentaControllerTest {
    @Mock UsuarioService usuarioService;
    private MockMvc mvc;

    @BeforeEach
    void iniciar() {
        mvc = construir(new CuentaController(usuarioService));
        autenticarComo(Datos.usuario(5L));
    }

    @AfterEach
    void limpiar() {
        cerrarSesion();
    }

    @Test
    @DisplayName("cambia la contraseña del usuario autenticado: 204")
    void cambiar() throws Exception {
        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"vieja-clave-1\",\"contrasenaNueva\":\"nueva-clave-1\"}"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UsuarioDetails> usuario = ArgumentCaptor.forClass(UsuarioDetails.class);
        ArgumentCaptor<CambioContrasenaRequest> request = ArgumentCaptor.forClass(CambioContrasenaRequest.class);
        verify(usuarioService).cambiarContrasena(usuario.capture(), request.capture());
        assertThat(usuario.getValue().getIdUsuario()).isEqualTo(5L);
        assertThat(request.getValue().contrasenaNueva()).isEqualTo("nueva-clave-1");
    }

    @Test
    @DisplayName("una nueva contraseña corta se rechaza con 400 antes de llegar al servicio")
    void contrasenaCorta() throws Exception {
        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"vieja-clave-1\",\"contrasenaNueva\":\"corta\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(usuarioService);
    }

    @Test
    @DisplayName("si la actual es incorrecta el servicio responde 400 (no 401, para no cerrar la sesión en Angular)")
    void actualIncorrecta() throws Exception {
        doThrow(ApiException.solicitudInvalida("incorrecta"))
                .when(usuarioService).cambiarContrasena(any(UsuarioDetails.class), any(CambioContrasenaRequest.class));

        mvc.perform(put("/api/auth/password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contrasenaActual\":\"equivocada\",\"contrasenaNueva\":\"nueva-clave-1\"}"))
                .andExpect(status().isBadRequest());
    }
}
