package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsuarioDetails implements UserDetails {
    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getIdUsuario() {
        return usuario.getIdUsuario();
    }

    public boolean esAdmin() {
        return usuario.getRol() == com.novalectura.novalectura_backend.entity.enums.Rol.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getContrasena();
    }

    @Override
    public String getUsername() {
        return usuario.getCorreo();
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true; // O puedes retornar el valor de un campo booleano de tu entidad Usuario (ej. activo)
    }
}
