package com.novalectura.api.controller;

import com.novalectura.api.dto.RegistroRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.GlobalExceptionHandler;
import com.novalectura.api.model.Rol;
import com.novalectura.api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registro_valido_responde201() throws Exception {
        when(authService.registrar(any(RegistroRequest.class))).thenReturn(
                new UsuarioResponse(1L, "Ana", "Paz", "ana@mail.com", Rol.CLIENTE, null, null, true, null));

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"Ana","apellidos":"Paz","correo":"ana@mail.com","contrasena":"secreto123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void registro_datosInvalidos_responde400ConElDetalleDeCadaCampo() throws Exception {
        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"","apellidos":"Paz","correo":"no-es-correo","contrasena":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());
    }

    @Test
    void registro_correoDuplicado_responde409() throws Exception {
        when(authService.registrar(any(RegistroRequest.class)))
                .thenThrow(new ConflictoException("El correo ya está registrado"));

        mockMvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"Ana","apellidos":"Paz","correo":"ana@mail.com","contrasena":"secreto123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("El correo ya está registrado"));
    }

    @Test
    void login_credencialesIncorrectas_responde401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("mal"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ana@mail.com","contrasena":"incorrecta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_responde204() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }
}
