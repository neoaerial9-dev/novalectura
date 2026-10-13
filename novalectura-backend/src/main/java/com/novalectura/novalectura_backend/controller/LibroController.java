package com.novalectura.novalectura_backend.controller;

import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import com.novalectura.novalectura_backend.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class LibroController {
    private final LibroService libroService;

    @GetMapping
    public PageResponse<LibroResponse> listar(
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano,
            @RequestParam(required = false) String orden) {
        return libroService.buscar(null, idCategoria, pagina, tamano, orden);
    }

    @GetMapping("/search")
    public PageResponse<LibroResponse> buscar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long idCategoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano,
            @RequestParam(required = false) String orden) {
        return libroService.buscar(q, idCategoria, pagina, tamano, orden);
    }

    @GetMapping("/mine")
    public PageResponse<LibroResponse> misLibros(
            @AuthenticationPrincipal UsuarioDetails actual,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "12") int tamano) {
        return libroService.misLibros(actual, pagina, tamano);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LibroResponse crear(@AuthenticationPrincipal UsuarioDetails actual,
                               @Valid @RequestBody LibroRequest request) {
        return libroService.crear(actual, request);
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(@PathVariable Long id,
                                    @AuthenticationPrincipal UsuarioDetails actual,
                                    @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, actual, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetails actual) {
        libroService.eliminar(id, actual);
    }
}
