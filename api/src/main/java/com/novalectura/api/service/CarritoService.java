package com.novalectura.api.service;

import com.novalectura.api.dto.CarritoResponse;
import com.novalectura.api.exception.ConflictoException;
import com.novalectura.api.exception.RecursoNoEncontradoException;
import com.novalectura.api.exception.SolicitudInvalidaException;
import com.novalectura.api.model.Carrito;
import com.novalectura.api.model.DetalleCarrito;
import com.novalectura.api.model.Libro;
import com.novalectura.api.model.Usuario;
import com.novalectura.api.repository.CarritoRepository;
import com.novalectura.api.repository.LibroRepository;
import com.novalectura.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CarritoResponse obtenerCarrito(String correo) {
        return CarritoResponse.from(obtenerOCrear(correo));
    }

    @Transactional
    public CarritoResponse agregarLibro(String correo, Long libroId, int cantidad) {
        Carrito carrito = obtenerOCrear(correo);
        Libro libro = libroRepository.findByIdAndActivoTrue(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado"));
        if (libro.getVendedor().getId().equals(carrito.getUsuario().getId())) {
            throw new SolicitudInvalidaException("No puedes comprar un libro que tú publicaste");
        }
        DetalleCarrito existente = carrito.buscarDetalle(libroId).orElse(null);
        int nuevaCantidad = (existente == null ? 0 : existente.getCantidad()) + cantidad;
        validarStock(libro, nuevaCantidad);
        if (existente == null) {
            carrito.agregarDetalle(DetalleCarrito.builder().libro(libro).cantidad(cantidad).build());
        } else {
            existente.setCantidad(nuevaCantidad);
        }
        carritoRepository.save(carrito);
        return CarritoResponse.from(carrito);
    }

    @Transactional
    public CarritoResponse actualizarCantidad(String correo, Long libroId, int cantidad) {
        Carrito carrito = obtenerOCrear(correo);
        DetalleCarrito detalle = buscarDetalle(carrito, libroId);
        validarStock(detalle.getLibro(), cantidad);
        detalle.setCantidad(cantidad);
        carritoRepository.save(carrito);
        return CarritoResponse.from(carrito);
    }

    @Transactional
    public CarritoResponse eliminarLibro(String correo, Long libroId) {
        Carrito carrito = obtenerOCrear(correo);
        DetalleCarrito detalle = buscarDetalle(carrito, libroId);
        carrito.getDetalles().remove(detalle);
        carritoRepository.save(carrito);
        return CarritoResponse.from(carrito);
    }

    @Transactional
    public void vaciar(String correo) {
        Carrito carrito = obtenerOCrear(correo);
        carrito.getDetalles().clear();
        carritoRepository.save(carrito);
    }

    private Carrito obtenerOCrear(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return carritoRepository.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            Carrito nuevo = Carrito.builder().usuario(usuario).build();
            carritoRepository.save(nuevo);
            return nuevo;
        });
    }

    private DetalleCarrito buscarDetalle(Carrito carrito, Long libroId) {
        return carrito.buscarDetalle(libroId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El libro no está en el carrito"));
    }

    private void validarStock(Libro libro, int cantidad) {
        if (cantidad > libro.getStock()) {
            throw new ConflictoException("Stock insuficiente para '" + libro.getTitulo()
                    + "'. Disponible: " + libro.getStock());
        }
    }
}
