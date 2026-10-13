package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.entity.Usuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration vigencia;

    public JwtService(JwtProperties propiedades) {
        String secreto = propiedades.secret();
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException(
                    "Falta la clave JWT. Define la variable de entorno JWT_SECRET "
                            + "(genera una con: openssl rand -base64 32)");
        }
        byte[] bytes = Decoders.BASE64.decode(secreto);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes (256 bits) en Base64");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.vigencia = Duration.ofMinutes(propiedades.expirationMinutes());
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el correo (subject) si el token es válido y no expiró; vacío en cualquier otro caso. */
    public Optional<String> extraerCorreo(String token) {
        try {
            String correo = Jwts.parser()
                    .verifyWith(clave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Optional.ofNullable(correo);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
