package com.novalectura.novalectura_backend.testutil;

import com.novalectura.novalectura_backend.entity.Categoria;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import com.novalectura.novalectura_backend.entity.enums.Rol;

import java.math.BigDecimal;

public final class Datos {
    private Datos() {
    }

    public static Usuario usuario(Long id, Rol rol) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombres("Nombre" + id);
        u.setApellidos("Apellido" + id);
        u.setCorreo("user" + id + "@mail.com");
        u.setContrasena("hash-" + id);
        u.setRol(rol);
        return u;
    }

    public static Usuario usuario(Long id) {
        return usuario(id, Rol.USUARIO);
    }

    public static Categoria categoria(Long id, String nombre) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombre(nombre);
        return c;
    }

    public static Libro libro(Long id, Usuario vendedor, String precio, int stock) {
        Libro l = new Libro();
        l.setIdLibro(id);
        l.setTitulo("Libro " + id);
        l.setAutor("Autor " + id);
        l.setPrecio(new BigDecimal(precio));
        l.setStock(stock);
        l.setCondicion(CondicionLibro.BUENO);
        l.setCategoria(categoria(1L, "Ficción"));
        l.setVendedor(vendedor);
        l.setActivo(true);
        return l;
    }
}
