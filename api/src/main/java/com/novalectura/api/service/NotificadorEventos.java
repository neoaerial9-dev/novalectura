package com.novalectura.api.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Component
public class NotificadorEventos {

    private static final long TIMEOUT_MS = 30L * 60 * 1000;

    private final List<SseEmitter> emisores = new CopyOnWriteArrayList<>();

    public SseEmitter conectar() {
        SseEmitter emisor = new SseEmitter(TIMEOUT_MS);
        emisores.add(emisor);
        emisor.onCompletion(() -> emisores.remove(emisor));
        emisor.onTimeout(() -> emisores.remove(emisor));
        emisor.onError(error -> emisores.remove(emisor));
        try {
            emisor.send(SseEmitter.event().name("conectado").data("ok"));
        } catch (IOException | IllegalStateException e) {
            emisores.remove(emisor);
        }
        return emisor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alCambiar(CambioEvent evento) {
        publicar(evento.recurso());
    }

    public void publicar(RecursoCambiado recurso) {
        enviar(() -> SseEmitter.event().name(recurso.evento()).data("cambio"));
    }

    @Scheduled(fixedRate = 25_000)
    public void latido() {
        enviar(() -> SseEmitter.event().comment("latido"));
    }

    public int suscriptores() {
        return emisores.size();
    }

    private void enviar(Supplier<SseEmitter.SseEventBuilder> evento) {
        for (SseEmitter emisor : emisores) {
            try {
                emisor.send(evento.get());
            } catch (IOException | IllegalStateException e) {
                emisores.remove(emisor);
            }
        }
    }
}
