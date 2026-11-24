package com.novalectura.api.service;

import com.novalectura.api.dto.VentaResponse;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.DetallePedidoRepository;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<VentaResponse> listarMisVentas(String correo) {
        Usuario vendedor = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return detallePedidoRepository.buscarVentasDelVendedor(vendedor.getId())
                .stream().map(VentaResponse::from).toList();
    }
}
