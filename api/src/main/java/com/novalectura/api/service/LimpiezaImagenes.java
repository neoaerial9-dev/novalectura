package com.novalectura.api.service;

import com.novalectura.api.repository.LibroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class LimpiezaImagenes {

    private final AlmacenImagenes almacen;
    private final LibroRepository libroRepository;
    private final long retencionHoras;

    public LimpiezaImagenes(AlmacenImagenes almacen, LibroRepository libroRepository,
                            @Value("${app.uploads.retencion-horas}") long retencionHoras) {
        this.almacen = almacen;
        this.libroRepository = libroRepository;
        this.retencionHoras = retencionHoras;
    }

    @Scheduled(cron = "${app.uploads.limpieza-cron}")
    public int limpiar() {
        Set<String> enUso = new HashSet<>(libroRepository.findRutasDeImagenesPropias());
        int borradas = almacen.limpiarHuerfanas(enUso, Duration.ofHours(retencionHoras));
        if (borradas > 0) {
            log.info("Limpieza de imágenes: {} archivo(s) huérfano(s) eliminado(s)", borradas);
        }
        return borradas;
    }
}
