package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.CambioContrasenaRequest;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class CuentaController {
    private final UsuarioService usuarioService;

    @PutMapping("/password")
    public ResponseEntity<Void> cambiarContrasena(@AuthenticationPrincipal UsuarioDetails actual,
                                                  @Valid @RequestBody CambioContrasenaRequest request) {
        usuarioService.cambiarContrasena(actual, request);
        return ResponseEntity.noContent().build();
    }
}
