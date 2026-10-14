package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.response.*;
import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.PedidoService;
import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.novalectura.novalectura_backend.controller.MockMvcSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoController (/api/orders)")
class PedidoControllerTest {
    @Mock PedidoService pedidoService;
    private MockMvc mvc;

    private static final PedidoResponse PEDIDO = new PedidoResponse(
            1L, LocalDateTime.of(2026, 10, 1, 12, 0), new BigDecimal("20.00"), EstadoPedido.PENDIENTE,
            new CompradorResumen(5L, "Ana Quispe", "ana@mail.com"),
            List.of(new DetallePedidoResponse(10L, "Dune", 2, new BigDecimal("10.00"), new BigDecimal("20.00"),
                    new VendedorResumen(9L, "Luis Perez"))));

    @BeforeEach
    void iniciar() {
        mvc = construir(new PedidoController(pedidoService));
        autenticarComo(Datos.usuario(5L));
    }

    @AfterEach
    void limpiar() {
        cerrarSesion();
    }

    @Test
    @DisplayName("POST /api/orders registra el pedido desde el carrito (sin cuerpo): 201")
    void registrar() throws Exception {
        when(pedidoService.registrar(any(UsuarioDetails.class))).thenReturn(PEDIDO);

        mvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.comprador.correo").value("ana@mail.com"))
                .andExpect(jsonPath("$.detalles[0].vendedor.nombreCompleto").value("Luis Perez"));

        ArgumentCaptor<UsuarioDetails> captor = ArgumentCaptor.forClass(UsuarioDetails.class);
        verify(pedidoService).registrar(captor.capture());
        assertThat(captor.getValue().getIdUsuario()).isEqualTo(5L);
    }

    @Test
    @DisplayName("POST /api/orders con carrito vacío (400) o stock agotado (409): el error llega al cliente")
    void registrar_errores() throws Exception {
        when(pedidoService.registrar(any(UsuarioDetails.class)))
                .thenThrow(ApiException.solicitudInvalida("carrito vacio"))
                .thenThrow(ApiException.conflicto("sin stock"));

        mvc.perform(post("/api/orders")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/orders")).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/orders usa página 0 y tamaño 12 por defecto")
    void listar() throws Exception {
        when(pedidoService.listar(any(UsuarioDetails.class), eq(0), eq(12)))
                .thenReturn(new PageResponse<>(List.of(PEDIDO), 0, 12, 1, 1));

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].idPedido").value(1));
    }

    @Test
    @DisplayName("GET /api/orders acepta los parámetros de paginación")
    void listar_conPaginacion() throws Exception {
        when(pedidoService.listar(any(UsuarioDetails.class), eq(3), eq(20)))
                .thenReturn(new PageResponse<>(List.of(), 3, 20, 0, 0));

        mvc.perform(get("/api/orders").param("pagina", "3").param("tamano", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagina").value(3));
    }

    @Test
    @DisplayName("GET /api/orders/{id} devuelve el pedido")
    void obtener() throws Exception {
        when(pedidoService.obtener(any(UsuarioDetails.class), eq(1L))).thenReturn(PEDIDO);

        mvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(20.00));
    }

    @Test
    @DisplayName("GET /api/orders/{id} de un pedido ajeno o inexistente: 404")
    void obtener_noExiste() throws Exception {
        when(pedidoService.obtener(any(UsuarioDetails.class), eq(99L)))
                .thenThrow(ApiException.noEncontrado("no existe"));

        mvc.perform(get("/api/orders/99")).andExpect(status().isNotFound());
    }
}
