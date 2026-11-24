package com.novalectura.api.dto;

public record AuthResponse(String token, String tipo, UsuarioResponse usuario) {
}
