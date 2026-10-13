package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioResponse miPerfil(@AuthenticationPrincipal UsuarioDetails actual) {
        return usuarioService.miPerfil(actual);
    }

    @PutMapping("/me")
    public UsuarioResponse actualizarPerfil(@AuthenticationPrincipal UsuarioDetails actual,
                                            @Valid @RequestBody UsuarioPerfilRequest request) {
        return usuarioService.actualizarPerfil(actual, request);
    }

    @GetMapping
    public PageResponse<UsuarioResponse> listar(@RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "12") int tamano) {
        return usuarioService.listar(pagina, tamano);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }
}
