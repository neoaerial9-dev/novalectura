package com.novalectura.api.service;

import com.novalectura.api.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimpiezaImagenesTest {

    @Mock AlmacenImagenes almacen;
    @Mock LibroRepository libroRepository;

    @Test
    void limpiar_consideraEnUsoLasImagenesDeTodosLosLibros_yRespetaLaRetencionConfigurada() {
        when(libroRepository.findRutasDeImagenesPropias())
                .thenReturn(List.of("/api/imagenes/a.png", "/api/imagenes/b.jpg", "/api/imagenes/a.png"));
        when(almacen.limpiarHuerfanas(Set.of("/api/imagenes/a.png", "/api/imagenes/b.jpg"), Duration.ofHours(12)))
                .thenReturn(3);

        int borradas = new LimpiezaImagenes(almacen, libroRepository, 12).limpiar();

        assertThat(borradas).isEqualTo(3);
    }

    @Test
    void limpiar_siNoSePuedenLeerLosLibros_noBorraNada() {
        when(libroRepository.findRutasDeImagenesPropias()).thenThrow(new IllegalStateException("base caída"));

        assertThatThrownBy(() -> new LimpiezaImagenes(almacen, libroRepository, 24).limpiar())
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(almacen);
    }
}
