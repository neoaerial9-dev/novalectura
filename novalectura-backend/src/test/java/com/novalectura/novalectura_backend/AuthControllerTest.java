package com.novalectura.novalectura_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalectura.novalectura_backend.controller.AuthController;
import com.novalectura.novalectura_backend.dto.request.LoginRequest;
import com.novalectura.novalectura_backend.dto.request.RegistroRequest;
import com.novalectura.novalectura_backend.dto.response.AuthResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void registerCreatesUserAndReturnsToken() throws Exception {
        RegistroRequest request = new RegistroRequest(
                "Ana", "Torres", "ana@example.com", "password123");
        given(authService.registrar(any(RegistroRequest.class))).willReturn(authResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.correo").value("ana@example.com"));

        verify(authService).registrar(any(RegistroRequest.class));
    }

    @Test
    void loginReturnsToken() throws Exception {
        LoginRequest request = new LoginRequest("ana@example.com", "password123");
        given(authService.iniciarSesion(any(LoginRequest.class))).willReturn(authResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.usuario.idUsuario").value(1));

        verify(authService).iniciarSesion(any(LoginRequest.class));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
    }

    private AuthResponse authResponse() {
        UsuarioResponse usuario = new UsuarioResponse(
                1L,
                "Ana",
                "Torres",
                "ana@example.com",
                Rol.USUARIO,
                null,
                null,
                LocalDateTime.of(2026, 1, 1, 10, 0));
        return AuthResponse.bearer("jwt-token", usuario);
    }
}
