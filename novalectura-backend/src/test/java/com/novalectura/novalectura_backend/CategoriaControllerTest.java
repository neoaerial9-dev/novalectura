package com.novalectura.novalectura_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalectura.novalectura_backend.controller.CategoriaController;
import com.novalectura.novalectura_backend.dto.request.CategoriaRequest;
import com.novalectura.novalectura_backend.dto.response.CategoriaResponse;
import com.novalectura.novalectura_backend.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoriaService categoriaService;

    @Test
    void listCategoriesReturnsCategories() throws Exception {
        given(categoriaService.listar())
                .willReturn(List.of(new CategoriaResponse(1L, "Ficcion", "Novelas y cuentos")));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCategoria").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ficcion"));

        verify(categoriaService).listar();
    }

    @Test
    void createCategoryReturnsCreated() throws Exception {
        CategoriaRequest request = new CategoriaRequest("Historia", "Libros historicos");
        given(categoriaService.crear(any(CategoriaRequest.class)))
                .willReturn(new CategoriaResponse(2L, "Historia", "Libros historicos"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(2));

        verify(categoriaService).crear(any(CategoriaRequest.class));
    }

    @Test
    void updateCategoryReturnsCategory() throws Exception {
        CategoriaRequest request = new CategoriaRequest("Tecnologia", "Libros tecnicos");
        given(categoriaService.actualizar(eq(1L), any(CategoriaRequest.class)))
                .willReturn(new CategoriaResponse(1L, "Tecnologia", "Libros tecnicos"));

        mockMvc.perform(put("/api/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tecnologia"));

        verify(categoriaService).actualizar(eq(1L), any(CategoriaRequest.class));
    }

    @Test
    void deleteCategoryReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(categoriaService).eliminar(1L);
    }
}
