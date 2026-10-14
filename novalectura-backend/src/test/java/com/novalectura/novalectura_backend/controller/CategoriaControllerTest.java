package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.CategoriaRequest;
import com.novalectura.novalectura_backend.dto.response.CategoriaResponse;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.service.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.construir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaController (/api/categories)")
class CategoriaControllerTest {
    @Mock CategoriaService categoriaService;
    private MockMvc mvc;

    @BeforeEach
    void iniciar() {
        mvc = construir(new CategoriaController(categoriaService));
    }

    @Test
    @DisplayName("GET lista las categorías")
    void listar() throws Exception {
        when(categoriaService.listar()).thenReturn(List.of(
                new CategoriaResponse(1L, "Ciencia", null), new CategoriaResponse(2L, "Historia", "Pasado")));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ciencia"))
                .andExpect(jsonPath("$[1].idCategoria").value(2));
    }

    @Test
    @DisplayName("POST crea la categoría: 201")
    void crear() throws Exception {
        when(categoriaService.crear(any(CategoriaRequest.class))).thenReturn(new CategoriaResponse(3L, "Fantasia", null));

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Fantasia\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(3));
    }

    @Test
    @DisplayName("POST con nombre vacío: 400 y no llega al servicio")
    void crear_invalido() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").exists());

        verifyNoInteractions(categoriaService);
    }

    @Test
    @DisplayName("POST con un nombre repetido: 409")
    void crear_duplicada() throws Exception {
        when(categoriaService.crear(any(CategoriaRequest.class))).thenThrow(ApiException.conflicto("duplicada"));

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ciencia\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT actualiza la categoría indicada en la URL")
    void actualizar() throws Exception {
        when(categoriaService.actualizar(eq(3L), any(CategoriaRequest.class)))
                .thenReturn(new CategoriaResponse(3L, "Nueva", null));

        mvc.perform(put("/api/categories/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nueva"));
    }

    @Test
    @DisplayName("DELETE responde 204")
    void eliminar() throws Exception {
        mvc.perform(delete("/api/categories/3")).andExpect(status().isNoContent());

        verify(categoriaService).eliminar(3L);
    }

    @Test
    @DisplayName("DELETE de una categoría con libros: 409; inexistente: 404")
    void eliminar_errores() throws Exception {
        doThrow(ApiException.conflicto("tiene libros")).when(categoriaService).eliminar(1L);
        doThrow(ApiException.noEncontrado("no existe")).when(categoriaService).eliminar(2L);

        mvc.perform(delete("/api/categories/1")).andExpect(status().isConflict());
        mvc.perform(delete("/api/categories/2")).andExpect(status().isNotFound());
    }
}
