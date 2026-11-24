package com.novalectura.api.config;

import com.novalectura.api.model.Rol;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correo;
    private final String contrasena;

    public AdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                        @Value("${app.admin.correo:}") String correo,
                        @Value("${app.admin.contrasena:}") String contrasena) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (correo.isBlank() || contrasena.isBlank() || usuarioRepository.existsByRol(Rol.ADMINISTRADOR)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nombres("Administrador")
                .apellidos("Nova Lectura")
                .correo(correo.trim().toLowerCase())
                .contrasena(passwordEncoder.encode(contrasena))
                .rol(Rol.ADMINISTRADOR)
                .build());
        log.info("Administrador inicial creado: {}", correo);
    }
}
