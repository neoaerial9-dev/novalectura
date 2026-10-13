package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.LoginRequest;
import com.novalectura.novalectura_backend.dto.request.RegistroRequest;
import com.novalectura.novalectura_backend.dto.response.AuthResponse;
import com.novalectura.novalectura_backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    public AuthResponse iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return authService.iniciarSesion(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> cerrarSesion() {
        return ResponseEntity.noContent().build();
    }
}
