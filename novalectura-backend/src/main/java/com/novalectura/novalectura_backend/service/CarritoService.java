package com.novalectura.novalectura_backend.service;

import com.novalectura.novalectura_backend.dto.request.CarritoCantidadRequest;
import com.novalectura.novalectura_backend.dto.request.CarritoItemRequest;
import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.entity.Carrito;
import com.novalectura.novalectura_backend.entity.DetalleCarrito;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.exception.ApiException;
import com.novalectura.novalectura_backend.mapper.CarritoMapper;
import com.novalectura.novalectura_backend.repository.CarritoRepository;
import com.novalectura.novalectura_backend.repository.LibroRepository;
import com.novalectura.novalectura_backend.repository.UsuarioRepository;
import com.novalectura.novalectura_backend.security.UsuarioDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoService {
    private final CarritoRepository carritos;
    private final LibroRepository libros;
    private final UsuarioRepository usuarios;
    private final CarritoMapper mapper;

    @Transactional
    public CarritoResponse obtener(UsuarioDetails actual) {
        Carrito carrito = obtenerOCrear(actual);
        sincronizar(carrito);
        return responder(carrito);
    }

    @Transactional
    public CarritoResponse agregar(UsuarioDetails actual, CarritoItemRequest request) {
        Libro libro = libros.findByIdLibroAndActivoTrue(request.idLibro())
                .orElseThrow(() -> ApiException.noEncontrado("Libro no encontrado"));

        if (libro.getVendedor().getIdUsuario().equals(actual.getIdUsuario())) {
            throw ApiException.solicitudInvalida("No puedes comprar tu propio libro");
        }

        Carrito carrito = obtenerOCrear(actual);
        sincronizar(carrito);

        int yaEnCarrito = carrito.getDetalles().stream()
                .filter(d -> d.getLibro().getIdLibro().equals(libro.getIdLibro()))
                .mapToInt(DetalleCarrito::getCantidad)
                .sum();
        validarStock(libro, yaEnCarrito + request.cantidad());

        carrito.agregarLibro(libro, request.cantidad());
        return responder(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(UsuarioDetails actual, Long idDetalle,
                                              CarritoCantidadRequest request) {
        Carrito carrito = obtenerOCrear(actual);
        sincronizar(carrito);

        DetalleCarrito detalle = buscarDetalle(carrito, idDetalle);
        validarStock(detalle.getLibro(), request.cantidad());

        carrito.actualizarCantidad(idDetalle, request.cantidad());
        return responder(carrito);
    }

    @Transactional
    public CarritoResponse eliminarLibro(UsuarioDetails actual, Long idDetalle) {
        Carrito carrito = obtenerOCrear(actual);
        if (!carrito.eliminarLibro(idDetalle)) {
            throw ApiException.noEncontrado("El libro no está en tu carrito");
        }
        sincronizar(carrito);
        return responder(carrito);
    }

    @Transactional
    public CarritoResponse vaciar(UsuarioDetails actual) {
        Carrito carrito = obtenerOCrear(actual);
        carrito.vaciarCarrito();
        return responder(carrito);
    }

    private Carrito obtenerOCrear(UsuarioDetails actual) {
        return carritos.findByUsuario_IdUsuario(actual.getIdUsuario())
                .orElseGet(() -> {
                    Carrito nuevo = new Carrito();
                    nuevo.setUsuario(usuarios.getReferenceById(actual.getIdUsuario()));
                    return carritos.save(nuevo);
                });
    }

    private void sincronizar(Carrito carrito) {
        carrito.getDetalles().removeIf(d -> !d.getLibro().isActivo());
        carrito.getDetalles().forEach(d -> d.setPrecioUnitario(d.getLibro().getPrecio()));
        carrito.calcularTotal();
    }

    private CarritoResponse responder(Carrito carrito) {
        carritos.saveAndFlush(carrito);
        return mapper.toResponse(carrito);
    }

    private DetalleCarrito buscarDetalle(Carrito carrito, Long idDetalle) {
        return carrito.getDetalles().stream()
                .filter(d -> idDetalle.equals(d.getIdDetalle()))
                .findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("El libro no está en tu carrito"));
    }

    private void validarStock(Libro libro, int solicitado) {
        if (solicitado > libro.getStock()) {
            throw ApiException.conflicto("Stock insuficiente de \"%s\": disponibles %d"
                    .formatted(libro.getTitulo(), libro.getStock()));
        }
    }
}
