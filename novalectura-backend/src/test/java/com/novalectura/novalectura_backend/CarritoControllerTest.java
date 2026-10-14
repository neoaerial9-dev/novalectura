package com.novalectura.novalectura_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalectura.novalectura_backend.controller.CarritoController;
import com.novalectura.novalectura_backend.dto.request.CarritoCantidadRequest;
import com.novalectura.novalectura_backend.dto.request.CarritoItemRequest;
import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.dto.response.DetalleCarritoResponse;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.CarritoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarritoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarritoService carritoService;

    @Test
    void getCartReturnsCart() throws Exception {
        given(carritoService.obtener(nullable(UsuarioDetails.class))).willReturn(cartResponse());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1))
                .andExpect(jsonPath("$.detalles[0].idLibro").value(10));

        verify(carritoService).obtener(nullable(UsuarioDetails.class));
    }

    @Test
    void addCartItemReturnsCreatedCart() throws Exception {
        CarritoItemRequest request = new CarritoItemRequest(10L, 2);
        given(carritoService.agregar(nullable(UsuarioDetails.class), any(CarritoItemRequest.class)))
                .willReturn(cartResponse());

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(160.00));

        verify(carritoService).agregar(nullable(UsuarioDetails.class), any(CarritoItemRequest.class));
    }

    @Test
    void updateCartItemQuantityReturnsCart() throws Exception {
        CarritoCantidadRequest request = new CarritoCantidadRequest(3);
        given(carritoService.actualizarCantidad(
                nullable(UsuarioDetails.class), eq(5L), any(CarritoCantidadRequest.class)))
                .willReturn(cartResponse());

        mockMvc.perform(put("/api/cart/items/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1));

        verify(carritoService).actualizarCantidad(
                nullable(UsuarioDetails.class), eq(5L), any(CarritoCantidadRequest.class));
    }

    @Test
    void deleteCartItemReturnsCart() throws Exception {
        given(carritoService.eliminarLibro(nullable(UsuarioDetails.class), eq(5L)))
                .willReturn(cartResponse());

        mockMvc.perform(delete("/api/cart/items/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1));

        verify(carritoService).eliminarLibro(nullable(UsuarioDetails.class), eq(5L));
    }

    @Test
    void clearCartReturnsCart() throws Exception {
        given(carritoService.vaciar(nullable(UsuarioDetails.class)))
                .willReturn(new CarritoResponse(1L, BigDecimal.ZERO, List.of()));

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles").isEmpty());

        verify(carritoService).vaciar(nullable(UsuarioDetails.class));
    }

    private CarritoResponse cartResponse() {
        return new CarritoResponse(
                1L,
                new BigDecimal("160.00"),
                List.of(new DetalleCarritoResponse(
                        5L,
                        10L,
                        "Clean Code",
                        "clean-code.jpg",
                        5,
                        2,
                        new BigDecimal("80.00"),
                        new BigDecimal("160.00"))));
    }
}
