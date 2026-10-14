package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Libro")
class LibroTest {
    private Libro libro;

    @BeforeEach
    void iniciar() {
        libro = Datos.libro(1L, Datos.usuario(2L), "10.00", 5);
    }

    @Test
    @DisplayName("un libro nuevo nace activo y con stock 1 (libro usado: normalmente un ejemplar)")
    void valoresPorDefecto() {
        Libro nuevo = new Libro();
        assertThat(nuevo.isActivo()).isTrue();
        assertThat(nuevo.getStock()).isEqualTo(1);
    }

    @Test
    @DisplayName("descontar stock resta la cantidad pedida")
    void descontarStock_resta() {
        libro.descontarStock(2);
        assertThat(libro.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("se puede llevar todo el stock; queda en 0 y deja de estar disponible")
    void descontarStock_todo() {
        libro.descontarStock(5);
        assertThat(libro.getStock()).isZero();
        assertThat(libro.estaDisponible()).isFalse();
    }

    @Test
    @DisplayName("no permite descontar más de lo que hay y deja el stock intacto")
    void descontarStock_insuficiente() {
        assertThatThrownBy(() -> libro.descontarStock(6))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente")
                .hasMessageContaining(libro.getTitulo());
        assertThat(libro.getStock()).isEqualTo(5);
    }

    @ParameterizedTest(name = "cantidad {0} es inválida")
    @ValueSource(ints = {0, -1, -10})
    @DisplayName("la cantidad a descontar debe ser positiva")
    void descontarStock_cantidadInvalida(int cantidad) {
        assertThatThrownBy(() -> libro.descontarStock(cantidad))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(libro.getStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("está disponible solo si está activo y tiene stock")
    void estaDisponible() {
        assertThat(libro.estaDisponible()).isTrue();

        libro.setActivo(false);
        assertThat(libro.estaDisponible()).as("dado de baja").isFalse();

        libro.setActivo(true);
        libro.setStock(0);
        assertThat(libro.estaDisponible()).as("sin stock").isFalse();
    }

    @Test
    @DisplayName("al persistir se registra la fecha de publicación")
    void prePersist_asignaFecha() {
        assertThat(libro.getFechaPublicacion()).isNull();
        libro.alCrear();
        assertThat(libro.getFechaPublicacion()).isNotNull();
    }
}
