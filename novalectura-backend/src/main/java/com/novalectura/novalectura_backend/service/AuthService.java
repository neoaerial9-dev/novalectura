package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.LoginRequest;
import com.novalectura.novalectura_backend.dto.request.RegistroRequest;
import com.novalectura.novalectura_backend.dto.response.AuthResponse;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.UsuarioMapper;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.JwtService;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String correo = normalizar(request.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw ApiException.conflicto("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setContrasena(encoder.encode(request.contrasena()));
        usuario.setRol(Rol.USUARIO);
        usuarios.save(usuario);

        return respuesta(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse iniciarSesion(LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        normalizar(request.correo()), request.contrasena()));

        Usuario usuario = ((UsuarioDetails) autenticacion.getPrincipal()).getUsuario();
        return respuesta(usuario);
    }

    private AuthResponse respuesta(Usuario usuario) {
        return AuthResponse.bearer(jwtService.generarToken(usuario), usuarioMapper.toResponse(usuario));
    }

    private static String normalizar(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
