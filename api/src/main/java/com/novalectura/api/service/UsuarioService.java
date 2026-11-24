package com.novalectura.api.service;

import com.novalectura.api.dto.CambioContrasenaRequest;
import com.novalectura.api.dto.PerfilRequest;
import com.novalectura.api.dto.UsuarioResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventos;

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::from);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return UsuarioResponse.from(usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String correo) {
        return UsuarioResponse.from(buscarPorCorreo(correo));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(String correo, PerfilRequest solicitud) {
        Usuario usuario = buscarPorCorreo(correo);
        usuario.setNombres(solicitud.nombres().trim());
        usuario.setApellidos(solicitud.apellidos().trim());
        usuario.setTelefono(solicitud.telefono());
        usuario.setDireccionEnvio(solicitud.direccionEnvio());
        UsuarioResponse respuesta = UsuarioResponse.from(usuarioRepository.save(usuario));
        eventos.publishEvent(new CambioEvent(RecursoCambiado.USUARIOS));
        return respuesta;
    }

    @Transactional
    public void cambiarContrasena(String correo, CambioContrasenaRequest solicitud) {
        Usuario usuario = buscarPorCorreo(correo);
        if (!passwordEncoder.matches(solicitud.actual(), usuario.getContrasena())) {
            throw new SolicitudInvalidaException("La contraseña actual no es correcta");
        }
        usuario.setContrasena(passwordEncoder.encode(solicitud.nueva()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
