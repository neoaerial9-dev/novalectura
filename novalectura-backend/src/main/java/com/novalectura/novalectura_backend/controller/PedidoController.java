package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.PedidoResponse;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse registrar(@AuthenticationPrincipal UsuarioDetails actual) {
        return pedidoService.registrar(actual);
    }

    @GetMapping
    public PageResponse<PedidoResponse> listar(@AuthenticationPrincipal UsuarioDetails actual,
                                               @RequestParam(defaultValue = "0") int pagina,
                                               @RequestParam(defaultValue = "12") int tamano) {
        return pedidoService.listar(actual, pagina, tamano);
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@AuthenticationPrincipal UsuarioDetails actual, @PathVariable Long id) {
        return pedidoService.obtener(actual, id);
    }
}
