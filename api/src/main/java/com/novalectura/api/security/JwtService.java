package com.novalectura.api.security;

import com.novalectura.api.model.Rol;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_ROL = "rol";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
                      @Value("${app.jwt.expiration-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(String correo, Rol rol) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(correo)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expiracionMs))
                .signWith(clave)
                .compact();
    }

    public String extraerCorreo(String token) {
        return Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extraerRol(String token) {
        return Jwts.parser().verifyWith(clave).build()
                .parseSignedClaims(token).getPayload().get(CLAIM_ROL, String.class);
    }

    public boolean esTokenValido(String token) {
        try {
            Jwts.parser().verifyWith(clave).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
