package com.novalectura.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class NotificadorEventosTest {

    private final NotificadorEventos notificador = new NotificadorEventos();

    @Test
    void conectar_registraAlSuscriptor() {
        SseEmitter emisor = notificador.conectar();

        assertThat(emisor).isNotNull();
        assertThat(notificador.suscriptores()).isEqualTo(1);
    }

    @Test
    void conectar_variosClientes_todosQuedanRegistrados() {
        notificador.conectar();
        notificador.conectar();

        assertThat(notificador.suscriptores()).isEqualTo(2);
    }

    @Test
    void publicar_sinSuscriptores_noFalla() {
        assertThatCode(() -> notificador.publicar(RecursoCambiado.CATALOGO)).doesNotThrowAnyException();
    }

    @Test
    void publicar_conSuscriptoresVivos_losMantiene() {
        notificador.conectar();

        notificador.publicar(RecursoCambiado.PEDIDOS);

        assertThat(notificador.suscriptores()).isEqualTo(1);
    }

    @Test
    void publicar_descartaALosClientesQueYaSeDesconectaron() {
        SseEmitter desconectado = notificador.conectar();
        notificador.conectar();
        desconectado.complete();

        notificador.publicar(RecursoCambiado.PEDIDOS);

        assertThat(notificador.suscriptores()).isEqualTo(1);
    }

    @Test
    void latido_tambienLimpiaLasConexionesMuertas() {
        notificador.conectar().complete();

        notificador.latido();

        assertThat(notificador.suscriptores()).isZero();
    }

    @Test
    void alCambiar_publicaElAvisoDelRecurso() {
        SseEmitter emisor = notificador.conectar();
        emisor.complete();

        notificador.alCambiar(new CambioEvent(RecursoCambiado.USUARIOS));

        assertThat(notificador.suscriptores()).isZero();
    }
}
