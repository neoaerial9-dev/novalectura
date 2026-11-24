package com.novalectura.api.controller;

import com.novalectura.api.exception.GlobalExceptionHandler;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.service.AlmacenImagenes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImagenControllerTest {

    @Mock AlmacenImagenes almacen;
    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ImagenController(almacen))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private MockMultipartFile archivo() {
        return new MockMultipartFile("archivo", "portada.png", "image/png", new byte[]{1, 2, 3});
    }

    @Test
    void subir_devuelve201ConLaRutaDeLaImagen() throws Exception {
        when(almacen.guardar(any())).thenReturn("/api/imagenes/abc.png");

        mockMvc.perform(multipart("/api/imagenes").file(archivo()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruta").value("/api/imagenes/abc.png"));
    }

    @Test
    void subir_archivoInvalido_devuelve400ConElMotivo() throws Exception {
        when(almacen.guardar(any())).thenThrow(new SolicitudInvalidaException("Solo se permiten imágenes JPG, PNG o WEBP"));

        mockMvc.perform(multipart("/api/imagenes").file(archivo()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Solo se permiten imágenes JPG, PNG o WEBP"));
    }

    @Test
    void obtener_devuelveLosBytesConSuTipoYCacheLargo() throws Exception {
        when(almacen.cargar("abc.png")).thenReturn(new AlmacenImagenes.ImagenLeida(new byte[]{1, 2, 3}, "image/png"));

        mockMvc.perform(get("/api/imagenes/abc.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=")));
    }

    @Test
    void obtener_imagenInexistente_devuelve404() throws Exception {
        when(almacen.cargar("nada.png")).thenThrow(new RecursoNoEncontradoException("Imagen no encontrada"));

        mockMvc.perform(get("/api/imagenes/nada.png")).andExpect(status().isNotFound());
    }
}
