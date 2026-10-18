package com.novalectura.api.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UsuarioTest {
    @Test
    @DisplayName("Crear perfectamente un usuario con rol CLIENTE")
    void crearUsuarioCliente() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(3L);
        usuario.setNombres("Juan Luis");
        usuario.setApellidos("Perez Obrador");
        usuario.setCorreo("j1023403@gmail.com");
        usuario.setPassword("qwerty");
        usuario.setRol("CLIENTE");
        assertEquals(3L, usuario.getIdUsuario());
        assertEquals("Juan Luis", usuario.getNombres());
        assertEquals("Perez Obrador", usuario.getApellidos());
        assertEquals("j1023403@gmail.com", usuario.getCorreo());
        assertEquals("qwerty", usuario.getPassword());
        assertNotEquals("ADMIN", usuario.getRol());
        assertEquals("CLIENTE", usuario.getRol());
    }
}
