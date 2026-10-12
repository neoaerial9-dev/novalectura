package com.novalectura.novalectura_backend.dto.response;

public record AuthResponse(String token, String tipo, UsuarioResponse usuario) {
    public static AuthResponse bearer(String token, UsuarioResponse usuario) {
        return new AuthResponse(token, "Bearer", usuario);
    }
}
