package com.novalectura.api.service;

import com.novalectura.api.dto.AuthResponse;
import com.novalectura.api.dto.LoginRequest;
import com.novalectura.api.dto.RegistroRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.UsuarioRepository;
import com.novalectura.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public UsuarioResponse registrar(RegistroRequest solicitud) {
        String correo = normalizar(solicitud.correo());
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ConflictoException("El correo ya está registrado");
        }
        Usuario usuario = Usuario.builder()
                .nombres(solicitud.nombres().trim())
                .apellidos(solicitud.apellidos().trim())
                .correo(correo)
                .contrasena(passwordEncoder.encode(solicitud.contrasena()))
                .rol(Rol.CLIENTE)
                .build();
        UsuarioResponse respuesta = UsuarioResponse.from(usuarioRepository.save(usuario));
        eventos.publishEvent(new CambioEvent(RecursoCambiado.USUARIOS));
        return respuesta;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest solicitud) {
        String correo = normalizar(solicitud.correo());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(correo, solicitud.contrasena()));
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        String token = jwtService.generarToken(usuario.getCorreo(), usuario.getRol());
        return new AuthResponse(token, "Bearer", UsuarioResponse.from(usuario));
    }

    private String normalizar(String correo) {
        return correo.trim().toLowerCase();
    }
}
