package com.novalectura.novalectura_backend.config;

import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.entity.enums.Rol;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Slf4j
@Component
public class AdminInitializer implements ApplicationRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String correo;
    private final String contrasena;

    public AdminInitializer(UsuarioRepository usuarios,
                            PasswordEncoder encoder,
                            @Value("${app.admin.correo:}") String correo,
                            @Value("${app.admin.contrasena:}") String contrasena) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.correo = correo;
        this.contrasena = contrasena;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (correo.isBlank() || contrasena.isBlank()) {
            log.warn("ADMIN_CORREO / ADMIN_CONTRASENA no definidos: no se crea usuario administrador.");
            return;
        }
        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByCorreo(correoNormalizado)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombres("Administrador");
        admin.setApellidos("Sistema");
        admin.setCorreo(correoNormalizado);
        admin.setContrasena(encoder.encode(contrasena));
        admin.setRol(Rol.ADMIN);
        usuarios.save(admin);
        log.info("Usuario administrador creado: {}", correoNormalizado);
    }
}
