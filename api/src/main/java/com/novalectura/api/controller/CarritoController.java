package com.novalectura.api.controller;

import com.novalectura.api.dto.CantidadRequest;
import com.novalectura.api.dto.CarritoResponse;
import com.novalectura.api.dto.ItemCarritoRequest;
import com.novalectura.api.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public CarritoResponse obtener(Principal principal) {
        return carritoService.obtenerCarrito(principal.getName());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CarritoResponse agregar(Principal principal, @Valid @RequestBody ItemCarritoRequest solicitud) {
        return carritoService.agregarLibro(principal.getName(), solicitud.libroId(), solicitud.cantidad());
    }

    @PutMapping("/items/{libroId}")
    public CarritoResponse actualizarCantidad(Principal principal, @PathVariable Long libroId,
                                              @Valid @RequestBody CantidadRequest solicitud) {
        return carritoService.actualizarCantidad(principal.getName(), libroId, solicitud.cantidad());
    }

    @DeleteMapping("/items/{libroId}")
    public CarritoResponse eliminar(Principal principal, @PathVariable Long libroId) {
        return carritoService.eliminarLibro(principal.getName(), libroId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void vaciar(Principal principal) {
        carritoService.vaciar(principal.getName());
    }
}
