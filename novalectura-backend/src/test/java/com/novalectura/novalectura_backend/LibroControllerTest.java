package com.novalectura.novalectura_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalectura.novalectura_backend.controller.LibroController;
import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.CategoriaResponse;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.VendedorResumen;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibroController.class)
@AutoConfigureMockMvc(addFilters = false)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibroService libroService;

    @Test
    void listBooksReturnsPage() throws Exception {
        given(libroService.buscar(isNull(), isNull(), eq(0), eq(12), isNull()))
                .willReturn(page(bookResponse()));

        mockMvc.perform(get("/api/books")
                        .param("pagina", "0")
                        .param("tamano", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].idLibro").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1));

        verify(libroService).buscar(null, null, 0, 12, null);
    }

    @Test
    void getBookByIdReturnsBook() throws Exception {
        given(libroService.obtener(10L)).willReturn(bookResponse());

        mockMvc.perform(get("/api/books/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Clean Code"));

        verify(libroService).obtener(10L);
    }

    @Test
    void searchBooksUsesQueryParameter() throws Exception {
        given(libroService.buscar(eq("clean"), isNull(), eq(0), eq(12), isNull()))
                .willReturn(page(bookResponse()));

        mockMvc.perform(get("/api/books/search").param("q", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].autor").value("Robert C. Martin"));

        verify(libroService).buscar("clean", null, 0, 12, null);
    }

    @Test
    void createBookReturnsCreated() throws Exception {
        LibroRequest request = bookRequest();
        given(libroService.crear(nullable(UsuarioDetails.class), any(LibroRequest.class)))
                .willReturn(bookResponse());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLibro").value(10));

        verify(libroService).crear(nullable(UsuarioDetails.class), any(LibroRequest.class));
    }

    @Test
    void updateBookReturnsUpdatedBook() throws Exception {
        given(libroService.actualizar(eq(10L), nullable(UsuarioDetails.class), any(LibroRequest.class)))
                .willReturn(bookResponse());

        mockMvc.perform(put("/api/books/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idLibro").value(10));

        verify(libroService).actualizar(eq(10L), nullable(UsuarioDetails.class), any(LibroRequest.class));
    }

    @Test
    void deleteBookReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", 10L))
                .andExpect(status().isNoContent());

        verify(libroService).eliminar(eq(10L), nullable(UsuarioDetails.class));
    }

    private PageResponse<LibroResponse> page(LibroResponse book) {
        return new PageResponse<>(List.of(book), 0, 12, 1, 1);
    }

    private LibroRequest bookRequest() {
        return new LibroRequest(
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("80.00"),
                5,
                "Buenas practicas de programacion",
                "clean-code.jpg",
                CondicionLibro.MUY_BUENO,
                1L);
    }

    private LibroResponse bookResponse() {
        return new LibroResponse(
                10L,
                "Clean Code",
                "Robert C. Martin",
                new BigDecimal("80.00"),
                5,
                "Buenas practicas de programacion",
                "clean-code.jpg",
                CondicionLibro.MUY_BUENO,
                LocalDateTime.of(2026, 1, 1, 10, 0),
                new CategoriaResponse(1L, "Tecnologia", "Libros tecnicos"),
                new VendedorResumen(2L, "Carlos Ruiz"));
    }
}
