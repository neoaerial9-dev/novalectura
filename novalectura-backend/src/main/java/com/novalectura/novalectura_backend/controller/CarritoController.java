package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.CarritoCantidadRequest;
import com.novalectura.novalectura_backend.dto.request.CarritoItemRequest;
import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CarritoController {
    private final CarritoService carritoService;

    @GetMapping
    public CarritoResponse obtener(@AuthenticationPrincipal UsuarioDetails actual) {
        return carritoService.obtener(actual);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CarritoResponse agregar(@AuthenticationPrincipal UsuarioDetails actual,
                                   @Valid @RequestBody CarritoItemRequest request) {
        return carritoService.agregar(actual, request);
    }

    @PutMapping("/items/{id}")
    public CarritoResponse actualizarCantidad(@AuthenticationPrincipal UsuarioDetails actual,
                                              @PathVariable Long id,
                                              @Valid @RequestBody CarritoCantidadRequest request) {
        return carritoService.actualizarCantidad(actual, id, request);
    }

    @DeleteMapping("/items/{id}")
    public CarritoResponse eliminarLibro(@AuthenticationPrincipal UsuarioDetails actual,
                                         @PathVariable Long id) {
        return carritoService.eliminarLibro(actual, id);
    }

    @DeleteMapping
    public CarritoResponse vaciar(@AuthenticationPrincipal UsuarioDetails actual) {
        return carritoService.vaciar(actual);
    }
}
