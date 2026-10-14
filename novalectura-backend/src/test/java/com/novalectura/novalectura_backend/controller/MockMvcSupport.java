package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.exception.GlobalExceptionHandler;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

final class MockMvcSupport {
    private MockMvcSupport() {
    }

    static MockMvc construir(Object... controladores) {
        return MockMvcBuilders.standaloneSetup(controladores)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    static void autenticarComo(Usuario usuario) {
        UsuarioDetails detalles = new UsuarioDetails(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(detalles, null, detalles.getAuthorities()));
    }

    static void cerrarSesion() {
        SecurityContextHolder.clearContext();
    }
}
