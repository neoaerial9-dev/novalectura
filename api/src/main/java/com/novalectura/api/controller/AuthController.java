package com.novalectura.api.controller;

import com.novalectura.api.dto.AuthResponse;
import com.novalectura.api.dto.LoginRequest;
import com.novalectura.api.dto.RegistroRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registro(@Valid @RequestBody RegistroRequest solicitud) {
        return authService.registrar(solicitud);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
    }
}
