package com.novalectura.api.service;

import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AlmacenImagenes {

    public static final long MAX_BYTES = 2L * 1024 * 1024;
    public static final String PREFIJO = "/api/imagenes/";

    private static final Pattern NOMBRE_VALIDO =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png|webp)$");

    public record ImagenLeida(byte[] contenido, String tipo) {
    }

    private final Path directorio;

    public AlmacenImagenes(@Value("${app.uploads.dir}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("Selecciona una imagen");
        }
        if (archivo.getSize() > MAX_BYTES) {
            throw new SolicitudInvalidaException("La imagen no puede superar los 2 MB");
        }
        byte[] contenido;
        try {
            contenido = archivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
        String extension = detectarExtension(contenido);
        if (extension == null) {
            throw new SolicitudInvalidaException("Solo se permiten imágenes JPG, PNG o WEBP");
        }
        String nombre = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(directorio);
            Files.write(directorio.resolve(nombre), contenido);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen", e);
        }
        return PREFIJO + nombre;
    }

    public ImagenLeida cargar(String nombre) {
        if (nombre == null || !NOMBRE_VALIDO.matcher(nombre).matches()) {
            throw new RecursoNoEncontradoException("Imagen no encontrada");
        }
        Path ruta = directorio.resolve(nombre);
        if (!Files.isRegularFile(ruta)) {
            throw new RecursoNoEncontradoException("Imagen no encontrada");
        }
        try {
            return new ImagenLeida(Files.readAllBytes(ruta), tipoDe(nombre));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la imagen", e);
        }
    }

    public void eliminar(String ruta) {
        if (ruta == null || !ruta.startsWith(PREFIJO)) {
            return;
        }
        String nombre = ruta.substring(PREFIJO.length());
        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(directorio.resolve(nombre));
        } catch (IOException e) {
            log.warn("No se pudo borrar la imagen {}", nombre, e);
        }
    }

    public int limpiarHuerfanas(Set<String> rutasEnUso, Duration antiguedadMinima) {
        if (!Files.isDirectory(directorio)) {
            return 0;
        }
        Instant limite = Instant.now().minus(antiguedadMinima);
        int borradas = 0;
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(directorio)) {
            for (Path archivo : archivos) {
                String nombre = archivo.getFileName().toString();
                if (!NOMBRE_VALIDO.matcher(nombre).matches() || rutasEnUso.contains(PREFIJO + nombre)) {
                    continue;
                }
                try {
                    if (Files.getLastModifiedTime(archivo).toInstant().isAfter(limite)) {
                        continue;
                    }
                    if (Files.deleteIfExists(archivo)) {
                        borradas++;
                    }
                } catch (IOException e) {
                    log.warn("No se pudo borrar la imagen huérfana {}", nombre, e);
                }
            }
        } catch (IOException e) {
            log.warn("No se pudo recorrer la carpeta de imágenes", e);
        }
        return borradas;
    }

    private static String detectarExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "webp";
        }
        return null;
    }

    private static String tipoDe(String nombre) {
        if (nombre.endsWith(".png")) {
            return "image/png";
        }
        if (nombre.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
