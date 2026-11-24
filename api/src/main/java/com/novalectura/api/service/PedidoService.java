package com.novalectura.api.service;

import com.novalectura.api.dto.PedidoResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.*;
import com.novalectura.api.repository.CarritoRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.PedidoRepository;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public PedidoResponse registrarPedido(String correo) {
        Usuario usuario = buscarUsuario(correo);
        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .filter(c -> !c.getDetalles().isEmpty())
                .orElseThrow(() -> new SolicitudInvalidaException("El carrito está vacío"));

        Pedido pedido = Pedido.builder().usuario(usuario).estado(EstadoPedido.REGISTRADO).build();
        List<DetalleCarrito> items = carrito.getDetalles().stream()
                .sorted(Comparator.comparing(d -> d.getLibro().getId()))
                .toList();
        for (DetalleCarrito item : items) {
            Libro libro = item.getLibro();
            if (libroRepository.descontarStock(libro.getId(), item.getCantidad()) == 0) {
                throw new ConflictoException("Stock insuficiente o libro no disponible: '" + libro.getTitulo() + "'");
            }
            pedido.agregarDetalle(DetallePedido.builder()
                    .libro(libro)
                    .cantidad(item.getCantidad())
                    .precioUnitario(libro.getPrecio())
                    .build());
        }
        pedidoRepository.save(pedido);
        carrito.getDetalles().clear();
        eventos.publishEvent(new CambioEvent(RecursoCambiado.CATALOGO));
        eventos.publishEvent(new CambioEvent(RecursoCambiado.PEDIDOS));
        return PedidoResponse.from(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMisPedidos(String correo) {
        Usuario usuario = buscarUsuario(correo);
        return pedidoRepository.findByUsuarioIdOrderByFechaPedidoDesc(usuario.getId())
                .stream().map(PedidoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listarTodos(Pageable pageable) {
        return pedidoRepository.findAll(pageable).map(PedidoResponse::from);
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long id, String correo) {
        Usuario solicitante = buscarUsuario(correo);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        boolean esAdmin = solicitante.getRol() == Rol.ADMINISTRADOR;
        if (!esAdmin && !pedido.getUsuario().getId().equals(solicitante.getId())) {
            throw new RecursoNoEncontradoException("Pedido no encontrado");
        }
        return PedidoResponse.from(pedido);
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }
}
