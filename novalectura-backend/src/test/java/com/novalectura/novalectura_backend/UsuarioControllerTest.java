package com.novalectura.novalectura_backend;

import com.novalectura.novalectura_backend.controller.UsuarioController;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    void listUsersReturnsPage() throws Exception {
        given(usuarioService.listar(eq(0), eq(12)))
                .willReturn(new PageResponse<>(List.of(userResponse()), 0, 12, 1, 1));

        mockMvc.perform(get("/api/users")
                        .param("pagina", "0")
                        .param("tamano", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].correo").value("admin@example.com"));

        verify(usuarioService).listar(0, 12);
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        given(usuarioService.obtener(1L)).willReturn(userResponse());

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUsuario").value(1))
                .andExpect(jsonPath("$.rol").value("ADMIN"));

        verify(usuarioService).obtener(1L);
    }

    private UsuarioResponse userResponse() {
        return new UsuarioResponse(
                1L,
                "Admin",
                "Nova",
                "admin@example.com",
                Rol.ADMIN,
                "999999999",
                "Av. Principal 123",
                LocalDateTime.of(2026, 1, 1, 8, 0));
    }
}
