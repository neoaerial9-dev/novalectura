package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.LibroService;
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

import java.math.BigDecimal;
import java.util.List;

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibroController (/api/books)")
class LibroControllerTest {
    @Mock LibroService libroService;
    private MockMvc mvc;

    private static final String CUERPO_VALIDO = """
            {"titulo":"Dune","autor":"Frank Herbert","precio":24.99,"stock":2,
             "descripcion":"Clasico","imagen":"dune.jpg","condicion":"BUENO","idCategoria":2}
            """;

    private static final PageResponse<LibroResponse> PAGINA_VACIA = new PageResponse<>(List.of(), 0, 12, 0, 0);

    private static LibroResponse libro() {
        return new LibroResponse(10L, "Dune", "Frank Herbert", new BigDecimal("24.99"), 2,
                null, null, CondicionLibro.BUENO, null, null, null);
    }

    @BeforeEach
    void iniciar() {
        mvc = construir(new LibroController(libroService));
        autenticarComo(Datos.usuario(5L));
    }

    @AfterEach
    void limpiar() {
        cerrarSesion();
    }

    @Test
    @DisplayName("GET /api/books usa página 0, tamaño 12 y sin filtros por defecto")
    void listar_valoresPorDefecto() throws Exception {
        when(libroService.buscar(isNull(), isNull(), eq(0), eq(12), isNull())).thenReturn(PAGINA_VACIA);

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(0))
                .andExpect(jsonPath("$.contenido.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/books/search pasa texto, categoría, paginación y orden al servicio")
    void buscar_conFiltros() throws Exception {
        when(libroService.buscar("dune", 3L, 2, 5, "precio_asc")).thenReturn(PAGINA_VACIA);

        mvc.perform(get("/api/books/search")
                        .param("q", "dune").param("idCategoria", "3")
                        .param("pagina", "2").param("tamano", "5").param("orden", "precio_asc"))
                .andExpect(status().isOk());

        verify(libroService).buscar("dune", 3L, 2, 5, "precio_asc");
    }

    @Test
    @DisplayName("GET /api/books/mine usa al usuario autenticado y no se confunde con /{id}")
    void misLibros() throws Exception {
        when(libroService.misLibros(any(UsuarioDetails.class), eq(0), eq(12))).thenReturn(PAGINA_VACIA);

        mvc.perform(get("/api/books/mine")).andExpect(status().isOk());

        ArgumentCaptor<UsuarioDetails> captor = ArgumentCaptor.forClass(UsuarioDetails.class);
        verify(libroService).misLibros(captor.capture(), eq(0), eq(12));
        assertThat(captor.getValue().getIdUsuario()).isEqualTo(5L);
    }

    @Test
    @DisplayName("GET /api/books/{id} devuelve el detalle")
    void obtener() throws Exception {
        when(libroService.obtener(10L)).thenReturn(libro());

        mvc.perform(get("/api/books/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dune"))
                .andExpect(jsonPath("$.condicion").value("BUENO"));
    }

    @Test
    @DisplayName("GET /api/books/{id} de un libro inexistente: 404")
    void obtener_noExiste() throws Exception {
        when(libroService.obtener(99L)).thenThrow(ApiException.noEncontrado("no existe"));

        mvc.perform(get("/api/books/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST crea el libro: 201, y el servicio recibe al usuario autenticado y los datos del cuerpo")
    void crear() throws Exception {
        when(libroService.crear(any(UsuarioDetails.class), any(LibroRequest.class))).thenReturn(libro());

        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idLibro").value(10));

        ArgumentCaptor<UsuarioDetails> usuario = ArgumentCaptor.forClass(UsuarioDetails.class);
        ArgumentCaptor<LibroRequest> request = ArgumentCaptor.forClass(LibroRequest.class);
        verify(libroService).crear(usuario.capture(), request.capture());
        assertThat(usuario.getValue().getIdUsuario()).isEqualTo(5L);
        assertThat(request.getValue().titulo()).isEqualTo("Dune");
        assertThat(request.getValue().precio()).isEqualByComparingTo("24.99");
        assertThat(request.getValue().idCategoria()).isEqualTo(2L);
    }

    @Test
    @DisplayName("POST con datos inválidos: 400 con el detalle por campo y sin llamar al servicio")
    void crear_invalido() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"autor\":\"X\",\"precio\":-1,\"stock\":1,\"condicion\":\"BUENO\",\"idCategoria\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists())
                .andExpect(jsonPath("$.errores.precio").exists());

        verifyNoInteractions(libroService);
    }

    @Test
    @DisplayName("POST con una condición que no existe en el enum: 400")
    void crear_condicionDesconocida() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Dune\",\"autor\":\"X\",\"precio\":5,\"stock\":1,\"condicion\":\"ROTO\",\"idCategoria\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(libroService);
    }

    @Test
    @DisplayName("PUT actualiza el libro de la URL con el usuario autenticado")
    void actualizar() throws Exception {
        when(libroService.actualizar(eq(10L), any(UsuarioDetails.class), any(LibroRequest.class))).thenReturn(libro());

        mvc.perform(put("/api/books/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dune"));
    }

    @Test
    @DisplayName("PUT sobre un libro ajeno: el 403 del servicio llega al cliente")
    void actualizar_ajeno() throws Exception {
        when(libroService.actualizar(eq(10L), any(UsuarioDetails.class), any(LibroRequest.class)))
                .thenThrow(ApiException.prohibido("no es tuyo"));

        mvc.perform(put("/api/books/10").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE responde 204 y delega en el servicio con el usuario autenticado")
    void eliminar() throws Exception {
        mvc.perform(delete("/api/books/10")).andExpect(status().isNoContent());

        ArgumentCaptor<UsuarioDetails> usuario = ArgumentCaptor.forClass(UsuarioDetails.class);
        verify(libroService).eliminar(eq(10L), usuario.capture());
        assertThat(usuario.getValue().getIdUsuario()).isEqualTo(5L);
    }

    @Test
    @DisplayName("DELETE sobre un libro ajeno: 403")
    void eliminar_ajeno() throws Exception {
        doThrow(ApiException.prohibido("no es tuyo")).when(libroService).eliminar(eq(10L), any(UsuarioDetails.class));

        mvc.perform(delete("/api/books/10")).andExpect(status().isForbidden());
    }
}
