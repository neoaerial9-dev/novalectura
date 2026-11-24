package com.novalectura.api.service;

import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlmacenImagenesTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P', 0};

    @TempDir Path directorio;
    private AlmacenImagenes almacen;

    @BeforeEach
    void preparar() {
        almacen = new AlmacenImagenes(directorio.toString());
    }

    private MockMultipartFile archivo(byte[] contenido, String nombre, String tipo) {
        return new MockMultipartFile("archivo", nombre, tipo, contenido);
    }

    @Test
    void guardar_png_devuelveRutaPublicaYEscribeElArchivo() throws Exception {
        String ruta = almacen.guardar(archivo(PNG, "portada.png", "image/png"));

        assertThat(ruta).matches("/api/imagenes/[0-9a-f-]{36}\\.png");
        String nombre = ruta.substring(AlmacenImagenes.PREFIJO.length());
        assertThat(Files.readAllBytes(directorio.resolve(nombre))).isEqualTo(PNG);
    }

    @Test
    void guardar_detectaJpgYWebpPorSuContenido() {
        assertThat(almacen.guardar(archivo(JPG, "a.bin", "application/octet-stream"))).endsWith(".jpg");
        assertThat(almacen.guardar(archivo(WEBP, "b.bin", "application/octet-stream"))).endsWith(".webp");
    }

    @Test
    void guardar_ignoraElNombreOriginalParaEvitarRutasPeligrosas() {
        String ruta = almacen.guardar(archivo(PNG, "../../etc/passwd.png", "image/png"));

        assertThat(ruta).doesNotContain("passwd").doesNotContain("..");
    }

    @Test
    void guardar_archivoQueNoEsImagen_aunqueDigaSerlo_lanzaSolicitudInvalida() {
        MockMultipartFile falso = archivo("<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8), "x.png", "image/png");

        assertThatThrownBy(() -> almacen.guardar(falso)).isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void guardar_archivoVacio_lanzaSolicitudInvalida() {
        assertThatThrownBy(() -> almacen.guardar(archivo(new byte[0], "x.png", "image/png")))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void guardar_archivoMayorAlLimite_lanzaSolicitudInvalida() {
        byte[] grande = new byte[(int) AlmacenImagenes.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, grande, 0, PNG.length);

        assertThatThrownBy(() -> almacen.guardar(archivo(grande, "x.png", "image/png")))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("2 MB");
    }

    @Test
    void cargar_devuelveElContenidoYElTipoSegunLaExtension() {
        String ruta = almacen.guardar(archivo(PNG, "a.png", "image/png"));

        AlmacenImagenes.ImagenLeida leida = almacen.cargar(ruta.substring(AlmacenImagenes.PREFIJO.length()));

        assertThat(leida.contenido()).isEqualTo(PNG);
        assertThat(leida.tipo()).isEqualTo("image/png");
    }

    @Test
    void cargar_nombreInvalidoOIntentoDeSalirDelDirectorio_lanzaNoEncontrado() {
        assertThatThrownBy(() -> almacen.cargar("../secreto.txt")).isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> almacen.cargar("cualquier-cosa.png")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void cargar_archivoInexistente_lanzaNoEncontrado() {
        assertThatThrownBy(() -> almacen.cargar("00000000-0000-0000-0000-000000000000.png"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void eliminar_borraElArchivoPropio() {
        String ruta = almacen.guardar(archivo(PNG, "a.png", "image/png"));
        String nombre = ruta.substring(AlmacenImagenes.PREFIJO.length());

        almacen.eliminar(ruta);

        assertThat(Files.exists(directorio.resolve(nombre))).isFalse();
    }

    @Test
    void eliminar_ignoraUrlsExternasNulasOMalFormadas() {
        almacen.eliminar(null);
        almacen.eliminar("https://sitio.com/foto.png");
        almacen.eliminar("/api/imagenes/../../x");
        // no debe lanzar excepciones
    }

    private String nombreDe(String ruta) {
        return ruta.substring(AlmacenImagenes.PREFIJO.length());
    }

    private void envejecer(String ruta, Duration antiguedad) throws Exception {
        Files.setLastModifiedTime(directorio.resolve(nombreDe(ruta)), FileTime.from(Instant.now().minus(antiguedad)));
    }

    @Test
    void limpiarHuerfanas_borraLasAntiguasQueNingunLibroUsa_yConservaLasEnUso() throws Exception {
        String enUso = almacen.guardar(archivo(PNG, "a.png", "image/png"));
        String huerfana = almacen.guardar(archivo(PNG, "b.png", "image/png"));
        envejecer(enUso, Duration.ofHours(48));
        envejecer(huerfana, Duration.ofHours(48));

        int borradas = almacen.limpiarHuerfanas(Set.of(enUso), Duration.ofHours(24));

        assertThat(borradas).isEqualTo(1);
        assertThat(Files.exists(directorio.resolve(nombreDe(enUso)))).isTrue();
        assertThat(Files.exists(directorio.resolve(nombreDe(huerfana)))).isFalse();
    }

    @Test
    void limpiarHuerfanas_respetaLasSubidasRecientes_porqueQuizaElFormularioSigueAbierto() {
        String reciente = almacen.guardar(archivo(PNG, "a.png", "image/png"));

        int borradas = almacen.limpiarHuerfanas(Set.of(), Duration.ofHours(24));

        assertThat(borradas).isZero();
        assertThat(Files.exists(directorio.resolve(nombreDe(reciente)))).isTrue();
    }

    @Test
    void limpiarHuerfanas_noTocaArchivosAjenosALasImagenes() throws Exception {
        Path ajeno = Files.writeString(directorio.resolve("notas.txt"), "no borrar");
        Files.setLastModifiedTime(ajeno, FileTime.from(Instant.now().minus(Duration.ofDays(30))));

        int borradas = almacen.limpiarHuerfanas(Set.of(), Duration.ofHours(24));

        assertThat(borradas).isZero();
        assertThat(Files.exists(ajeno)).isTrue();
    }

    @Test
    void limpiarHuerfanas_sinCarpetaDeImagenes_noHaceNada() {
        AlmacenImagenes vacio = new AlmacenImagenes(directorio.resolve("no-existe").toString());

        assertThat(vacio.limpiarHuerfanas(Set.of(), Duration.ZERO)).isZero();
    }
}
