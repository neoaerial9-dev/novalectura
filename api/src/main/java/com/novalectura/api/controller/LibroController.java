package com.novalectura.api.controller;

import com.novalectura.api.dto.LibroRequest;
import com.novalectura.api.dto.LibroResponse;
import com.novalectura.api.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public Page<LibroResponse> listar(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Long categoriaId,
                                      @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return libroService.listar(q, categoriaId, pageable);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibroResponse crear(Principal principal, @Valid @RequestBody LibroRequest solicitud) {
        return libroService.crear(principal.getName(), solicitud);
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(@PathVariable Long id, Principal principal,
                                    @Valid @RequestBody LibroRequest solicitud) {
        return libroService.actualizar(id, principal.getName(), solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, Principal principal) {
        libroService.eliminar(id, principal.getName());
    }
}
