package com.novalectura.api.controller;

import com.novalectura.api.dto.PedidoResponse;
import com.novalectura.api.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse registrar(Principal principal) {
        return pedidoService.registrarPedido(principal.getName());
    }

    @GetMapping("/mis-pedidos")
    public List<PedidoResponse> misPedidos(Principal principal) {
        return pedidoService.listarMisPedidos(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Page<PedidoResponse> listarTodos(
            @PageableDefault(size = 10, sort = "fechaPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return pedidoService.listarTodos(pageable);
    }

    @GetMapping("/{id}")
    public PedidoResponse obtener(@PathVariable Long id, Principal principal) {
        return pedidoService.obtenerPedido(id, principal.getName());
    }
}
