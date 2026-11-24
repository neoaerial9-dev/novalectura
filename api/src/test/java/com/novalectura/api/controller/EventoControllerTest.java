package com.novalectura.api.controller;

import com.novalectura.api.service.NotificadorEventos;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventoControllerTest {

    private final NotificadorEventos notificador = new NotificadorEventos();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new EventoController(notificador)).build();

    @Test
    void suscribirse_abreUnaConexionDeEventosYRegistraAlCliente() throws Exception {
        mockMvc.perform(get("/api/eventos").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andExpect(header().string("X-Accel-Buffering", "no"));

        assertThat(notificador.suscriptores()).isEqualTo(1);
    }
}
