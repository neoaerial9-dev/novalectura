package com.novalectura.novalectura_backend.mapper;

import com.novalectura.novalectura_backend.dto.response.CarritoResponse;
import com.novalectura.novalectura_backend.dto.response.DetalleCarritoResponse;
import com.novalectura.novalectura_backend.entity.Carrito;
import com.novalectura.novalectura_backend.entity.DetalleCarrito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarritoMapper {
    CarritoResponse toResponse(Carrito carrito);

    @Mapping(target = "idLibro", source = "libro.idLibro")
    @Mapping(target = "titulo", source = "libro.titulo")
    @Mapping(target = "imagen", source = "libro.imagen")
    @Mapping(target = "stockDisponible", source = "libro.stock")
    DetalleCarritoResponse toDetalleResponse(DetalleCarrito detalle);
}
