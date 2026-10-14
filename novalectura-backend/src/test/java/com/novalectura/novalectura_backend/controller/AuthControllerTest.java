package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.LoginRequest;
import com.novalectura.novalectura_backend.dto.request.RegistroRequest;
import com.novalectura.novalectura_backend.dto.response.AuthResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.construir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController (/api/auth)")
class AuthControllerTest {
    @Mock AuthService authService;
    private MockMvc mvc;

    private static final AuthResponse AUTENTICADO = AuthResponse.bearer("TOKEN-123",
            new UsuarioResponse(1L, "Ana", "Quispe", "ana@mail.com", Rol.USUARIO, null, null, null));

    @BeforeEach
    void iniciar() {
        mvc = construir(new AuthController(authService));
    }

    @Test
    @DisplayName("POST /register con datos válidos: 201, token y usuario (sin contraseña)")
    void registrar_ok() throws Exception {
        when(authService.registrar(any(RegistroRequest.class))).thenReturn(AUTENTICADO);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"Ana","apellidos":"Quispe","correo":"ana@mail.com","contrasena":"clave12345"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("TOKEN-123"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.usuario.contrasena").doesNotExist());
    }

    @Test
    @DisplayName("POST /register con datos inválidos: 400 con el detalle por campo y sin llamar al servicio")
    void registrar_invalido() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"","apellidos":"Quispe","correo":"no-es-correo","contrasena":"corta"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombres").exists())
                .andExpect(jsonPath("$.errores.correo").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /register sin cuerpo: 400")
    void registrar_sinCuerpo() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /register con un correo ya registrado: 409")
    void registrar_duplicado() throws Exception {
        when(authService.registrar(any(RegistroRequest.class)))
                .thenThrow(ApiException.conflicto("El correo ya esta registrado"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombres":"Ana","apellidos":"Quispe","correo":"ana@mail.com","contrasena":"clave12345"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /login correcto: 200 con token")
    void login_ok() throws Exception {
        when(authService.iniciarSesion(any(LoginRequest.class))).thenReturn(AUTENTICADO);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@mail.com\",\"contrasena\":\"clave12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("TOKEN-123"));
    }

    @Test
    @DisplayName("POST /login con credenciales incorrectas: 401")
    void login_incorrecto() throws Exception {
        when(authService.iniciarSesion(any(LoginRequest.class))).thenThrow(new BadCredentialsException("mal"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@mail.com\",\"contrasena\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /login con cuerpo incompleto: 400")
    void login_invalido() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"\",\"contrasena\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("POST /logout responde 204")
    void logout() throws Exception {
        mvc.perform(post("/api/auth/logout")).andExpect(status().isNoContent());
    }
}
