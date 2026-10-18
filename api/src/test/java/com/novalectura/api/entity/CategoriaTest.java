package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CategoriaTest {
    @Test
    @DisplayName("Crear de forma perfecta una categoria")
    void crearCategoriaPerfectamente() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Ficción");
        categoria.setDescripcion("Simulación de la realidad o la creación de mundos");
        assertEquals("Ficción", categoria.getNombre());
        assertEquals("Simulación de la realidad o la creación de mundos", categoria.getDescripcion());
    }
}
