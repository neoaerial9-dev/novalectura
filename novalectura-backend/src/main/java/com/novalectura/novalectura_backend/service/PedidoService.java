package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.response.PageResponse;
import com.novalectura.novalectura_backend.dto.response.PedidoResponse;
import com.novalectura.novalectura_backend.entity.Carrito;
import com.novalectura.novalectura_backend.entity.DetalleCarrito;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Pedido;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.PedidoMapper;
import com.novalectura.novalectura_backend.repository.CarritoRepository;
import com.novalectura.novalectura_backend.repository.PedidoRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private static final int TAMANO_MAXIMO = 50;

    private final PedidoRepository pedidos;
    private final CarritoRepository carritos;
    private final PedidoMapper mapper;

    @Transactional
    public PedidoResponse registrar(UsuarioDetails actual) {
        Carrito carrito = carritos.findByUsuario_IdUsuario(actual.getIdUsuario())
                .filter(c -> !c.getDetalles().isEmpty())
                .orElseThrow(() -> ApiException.solicitudInvalida("Tu carrito está vacío"));

        carrito.getDetalles().forEach(this::validarDisponibilidad);

        Pedido pedido = Pedido.registrarPedido(carrito);
        carrito.getDetalles().forEach(d -> d.getLibro().descontarStock(d.getCantidad()));
        pedidos.save(pedido);

        carrito.vaciarCarrito();

        return mapper.toResponse(pedido);
    }

    @Transactional(readOnly = true)
    public PageResponse<PedidoResponse> listar(UsuarioDetails actual, int pagina, int tamano) {
        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamano, 1), TAMANO_MAXIMO),
                Sort.by(Sort.Direction.DESC, "fechaPedido").and(Sort.by(Sort.Direction.DESC, "idPedido")));

        Page<Pedido> resultado = actual.esAdmin()
                ? pedidos.findAll(pageable)
                : pedidos.findByUsuario_IdUsuario(actual.getIdUsuario(), pageable);

        return PageResponse.de(resultado.map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(UsuarioDetails actual, Long idPedido) {
        Pedido pedido = pedidos.findWithDetallesByIdPedido(idPedido)
                .orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado"));

        boolean esPropio = pedido.getUsuario().getIdUsuario().equals(actual.getIdUsuario());
        if (!esPropio && !actual.esAdmin()) {
            throw ApiException.noEncontrado("Pedido no encontrado");
        }
        return mapper.toResponse(pedido);
    }

    private void validarDisponibilidad(DetalleCarrito detalle) {
        Libro libro = detalle.getLibro();
        if (!libro.isActivo()) {
            throw ApiException.conflicto("\"%s\" ya no está disponible".formatted(libro.getTitulo()));
        }
        if (detalle.getCantidad() > libro.getStock()) {
            throw ApiException.conflicto("Stock insuficiente de \"%s\": disponibles %d, en tu carrito %d"
                    .formatted(libro.getTitulo(), libro.getStock(), detalle.getCantidad()));
        }
    }
}
