package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LibroTest {
    @Test
    @DisplayName("Crear perfectamente un libro con todos los datos")
    void crearLibroPerfectamente() {
        Libro libro = new Libro();
        libro.setIdLibro(1L);
        libro.setTitulo("La divina comedia");
        libro.setAutor("Dante Alighieri");
        libro.setPrecio(50.0);
        libro.setStock(2);
        libro.setDescripcion("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation " +
                "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
                "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        libro.setImagen("https://samplelib.com/png/sample-photo-3840x2160.png");
        libro.setEstadoConservacion("Buen estado");
        libro.setDetallesUso("Edición de tapa blanda");
        assertEquals("La divina comedia", libro.getTitulo());
        assertEquals("Dante Alighieri", libro.getAutor());
        assertEquals(50.0, libro.getPrecio());
        assertEquals(2, libro.getStock());
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation " +
                "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in " +
                "voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
                "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", libro.getDescripcion());
        assertEquals("https://samplelib.com/png/sample-photo-3840x2160.png", libro.getImagen());
        assertEquals("Buen estado", libro.getEstadoConservacion());
        assertEquals("Edición de tapa blanda", libro.getDetallesUso());
    }

    @Test
    @DisplayName("Se debe asociar correctamente una categoría a cada libro")
    void asociarCategoria() {
        Libro libro = new Libro();
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(1L);
        categoria.setNombre("Ficción");
        libro.setCategoria(categoria);
        assertNotNull(libro.getCategoria());
        assertSame(categoria, libro.getCategoria());
    }
}
