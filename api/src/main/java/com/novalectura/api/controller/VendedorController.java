package com.novalectura.api.controller;

import com.novalectura.api.dto.LibroResponse;
import com.novalectura.api.dto.VentaResponse;
import com.novalectura.api.service.LibroService;
import com.novalectura.api.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class VendedorController {

    private final LibroService libroService;
    private final VentaService ventaService;

    @GetMapping("/mis-libros")
    public List<LibroResponse> misLibros(Principal principal) {
        return libroService.listarMisLibros(principal.getName());
    }

    @GetMapping("/mis-ventas")
    public List<VentaResponse> misVentas(Principal principal) {
        return ventaService.listarMisVentas(principal.getName());
    }
}
