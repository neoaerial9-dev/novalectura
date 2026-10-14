package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.CarritoCantidadRequest;
import com.novalectura.novalectura_backend.dto.request.CarritoItemRequest;
import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.dto.response.DetalleCarritoResponse;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.CarritoService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("CarritoController (/api/cart)")
class CarritoControllerTest {
    @Mock CarritoService carritoService;
    private MockMvc mvc;

    private static final CarritoResponse CARRITO = new CarritoResponse(1L, new BigDecimal("20.00"),
            List.of(new DetalleCarritoResponse(100L, 10L, "Dune", null, 3, 2,
                    new BigDecimal("10.00"), new BigDecimal("20.00"))));

    @BeforeEach
    void iniciar() {
        mvc = construir(new CarritoController(carritoService));
        autenticarComo(Datos.usuario(5L));
    }

    @AfterEach
    void limpiar() {
        cerrarSesion();
    }

    @Test
    @DisplayName("GET devuelve el carrito del usuario autenticado")
    void obtener() throws Exception {
        when(carritoService.obtener(any(UsuarioDetails.class))).thenReturn(CARRITO);

        mvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(20.00))
                .andExpect(jsonPath("$.detalles[0].titulo").value("Dune"))
                .andExpect(jsonPath("$.detalles[0].stockDisponible").value(3));

        ArgumentCaptor<UsuarioDetails> captor = ArgumentCaptor.forClass(UsuarioDetails.class);
        verify(carritoService).obtener(captor.capture());
        assertThat(captor.getValue().getIdUsuario()).isEqualTo(5L);
    }

    @Test
    @DisplayName("POST /items agrega un libro: 201 y devuelve el carrito actualizado")
    void agregar() throws Exception {
        when(carritoService.agregar(any(UsuarioDetails.class), any(CarritoItemRequest.class))).thenReturn(CARRITO);

        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLibro\":10,\"cantidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCarrito").value(1));

        ArgumentCaptor<CarritoItemRequest> captor = ArgumentCaptor.forClass(CarritoItemRequest.class);
        verify(carritoService).agregar(any(UsuarioDetails.class), captor.capture());
        assertThat(captor.getValue().idLibro()).isEqualTo(10L);
        assertThat(captor.getValue().cantidad()).isEqualTo(2);
    }

    @Test
    @DisplayName("POST /items con cantidad 0 o sin libro: 400 y no llega al servicio")
    void agregar_invalido() throws Exception {
        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLibro\":10,\"cantidad\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.cantidad").exists());
        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.idLibro").exists());

        verifyNoInteractions(carritoService);
    }

    @Test
    @DisplayName("POST /items sobre el propio libro (400) o sin stock (409): el error del servicio llega al cliente")
    void agregar_erroresDeNegocio() throws Exception {
        when(carritoService.agregar(any(UsuarioDetails.class), eq(new CarritoItemRequest(10L, 1))))
                .thenThrow(ApiException.solicitudInvalida("libro propio"));
        when(carritoService.agregar(any(UsuarioDetails.class), eq(new CarritoItemRequest(11L, 9))))
                .thenThrow(ApiException.conflicto("sin stock"));

        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLibro\":10,\"cantidad\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idLibro\":11,\"cantidad\":9}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /items/{id} cambia la cantidad de esa línea")
    void actualizarCantidad() throws Exception {
        when(carritoService.actualizarCantidad(any(UsuarioDetails.class), eq(100L), any(CarritoCantidadRequest.class)))
                .thenReturn(CARRITO);

        mvc.perform(put("/api/cart/items/100").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":3}"))
                .andExpect(status().isOk());

        ArgumentCaptor<CarritoCantidadRequest> captor = ArgumentCaptor.forClass(CarritoCantidadRequest.class);
        verify(carritoService).actualizarCantidad(any(UsuarioDetails.class), eq(100L), captor.capture());
        assertThat(captor.getValue().cantidad()).isEqualTo(3);
    }

    @Test
    @DisplayName("PUT /items/{id} con cantidad inválida: 400")
    void actualizarCantidad_invalida() throws Exception {
        mvc.perform(put("/api/cart/items/100").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(carritoService);
    }

    @Test
    @DisplayName("DELETE /items/{id} quita la línea y devuelve el carrito actualizado")
    void eliminarLibro() throws Exception {
        when(carritoService.eliminarLibro(any(UsuarioDetails.class), eq(100L))).thenReturn(CARRITO);

        mvc.perform(delete("/api/cart/items/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1));
    }

    @Test
    @DisplayName("DELETE /items/{id} de una línea que no está en mi carrito: 404")
    void eliminarLibro_noExiste() throws Exception {
        when(carritoService.eliminarLibro(any(UsuarioDetails.class), eq(999L)))
                .thenThrow(ApiException.noEncontrado("no esta"));

        mvc.perform(delete("/api/cart/items/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/cart vacía el carrito")
    void vaciar() throws Exception {
        when(carritoService.vaciar(any(UsuarioDetails.class)))
                .thenReturn(new CarritoResponse(1L, BigDecimal.ZERO, List.of()));

        mvc.perform(delete("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detalles.length()").value(0));
    }
}
