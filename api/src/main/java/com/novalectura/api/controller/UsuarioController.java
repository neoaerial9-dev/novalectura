package com.novalectura.api.controller;

import com.novalectura.api.dto.CambioContrasenaRequest;
import com.novalectura.api.dto.PerfilRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse miPerfil(Principal principal) {
        return usuarioService.obtenerPerfil(principal.getName());
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarMiPerfil(Principal principal, @Valid @RequestBody PerfilRequest solicitud) {
        return usuarioService.actualizarPerfil(principal.getName(), solicitud);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(Principal principal, @Valid @RequestBody CambioContrasenaRequest solicitud) {
        usuarioService.cambiarContrasena(principal.getName(), solicitud);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<UsuarioResponse> listar(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }
}
