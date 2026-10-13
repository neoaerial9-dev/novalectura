package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CambioContrasenaRequest;
import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.Usuario;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.UsuarioMapper;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private static final int TAMANO_MAXIMO = 50;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final UsuarioMapper mapper;

    @Transactional(readOnly = true)
    public UsuarioResponse miPerfil(UsuarioDetails actual) {
        return mapper.toResponse(buscar(actual.getIdUsuario()));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(UsuarioDetails actual, UsuarioPerfilRequest request) {
        Usuario usuario = buscar(actual.getIdUsuario());
        mapper.actualizarPerfil(request, usuario);

        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setTelefono(limpiar(request.telefono()));
        usuario.setDireccion(limpiar(request.direccion()));

        return mapper.toResponse(usuario);
    }

    @Transactional
    public void cambiarContrasena(UsuarioDetails actual, CambioContrasenaRequest request) {
        Usuario usuario = buscar(actual.getIdUsuario());

        if (!encoder.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw ApiException.solicitudInvalida("La contraseña actual es incorrecta");
        }
        if (request.contrasenaActual().equals(request.contrasenaNueva())) {
            throw ApiException.solicitudInvalida("La nueva contraseña debe ser distinta de la actual");
        }
        usuario.setContrasena(encoder.encode(request.contrasenaNueva()));
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(int pagina, int tamano) {
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 1), TAMANO_MAXIMO),
                Sort.by(Sort.Direction.DESC, "fechaRegistro").and(Sort.by(Sort.Direction.DESC, "idUsuario")));
        return PageResponse.de(usuarios.findAll(pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long idUsuario) {
        return mapper.toResponse(buscar(idUsuario));
    }

    private Usuario buscar(Long idUsuario) {
        return usuarios.findById(idUsuario)
                .orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }

    private static String limpiar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
