package com.novalectura.novalectura_backend;

import com.novalectura.novalectura_backend.controller.PedidoController;
import com.novalectura.novalectura_backend.dto.response.CompradorResumen;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.PedidoResponse;
import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@AutoConfigureMockMvc(addFilters = false)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @Test
    void createOrderReturnsCreated() throws Exception {
        given(pedidoService.registrar(nullable(UsuarioDetails.class))).willReturn(orderResponse());

        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido").value(20))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        verify(pedidoService).registrar(nullable(UsuarioDetails.class));
    }

    @Test
    void listOrdersReturnsPage() throws Exception {
        given(pedidoService.listar(nullable(UsuarioDetails.class), eq(0), eq(12)))
                .willReturn(new PageResponse<>(List.of(orderResponse()), 0, 12, 1, 1));

        mockMvc.perform(get("/api/orders")
                        .param("pagina", "0")
                        .param("tamano", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].idPedido").value(20));

        verify(pedidoService).listar(nullable(UsuarioDetails.class), eq(0), eq(12));
    }

    @Test
    void getOrderByIdReturnsOrder() throws Exception {
        given(pedidoService.obtener(nullable(UsuarioDetails.class), eq(20L))).willReturn(orderResponse());

        mockMvc.perform(get("/api/orders/{id}", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(160.00));

        verify(pedidoService).obtener(nullable(UsuarioDetails.class), eq(20L));
    }

    private PedidoResponse orderResponse() {
        return new PedidoResponse(
                20L,
                LocalDateTime.of(2026, 1, 2, 11, 30),
                new BigDecimal("160.00"),
                EstadoPedido.CONFIRMADO,
                new CompradorResumen(1L, "Ana Torres", "ana@example.com"),
                List.of());
    }
}
