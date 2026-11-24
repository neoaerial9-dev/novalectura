package com.novalectura.api.controller;

import com.novalectura.api.dto.ImagenSubidaResponse;
import com.novalectura.api.service.AlmacenImagenes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor
public class ImagenController {

    private final AlmacenImagenes almacen;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagenSubidaResponse subir(@RequestParam("archivo") MultipartFile archivo) {
        return new ImagenSubidaResponse(almacen.guardar(archivo));
    }

    @GetMapping("/{nombre}")
    public ResponseEntity<byte[]> obtener(@PathVariable String nombre) {
        AlmacenImagenes.ImagenLeida imagen = almacen.cargar(nombre);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagen.tipo()))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic().immutable())
                .header("X-Content-Type-Options", "nosniff")
                .body(imagen.contenido());
    }
}
