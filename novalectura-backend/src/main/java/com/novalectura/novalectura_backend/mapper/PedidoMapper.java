package com.novalectura.novalectura_backend.mapper;

import com.novalectura.novalectura_backend.dto.response.CompradorResumen;
import com.novalectura.novalectura_backend.dto.response.DetallePedidoResponse;
import com.novalectura.novalectura_backend.dto.response.PedidoResponse;
import com.novalectura.novalectura_backend.entity.DetallePedido;
import com.novalectura.novalectura_backend.entity.Pedido;
import com.novalectura.novalectura_backend.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = LibroMapper.class)
public interface PedidoMapper {
    @Mapping(target = "comprador", source = "usuario")
    PedidoResponse toResponse(Pedido pedido);

    @Mapping(target = "idLibro", source = "libro.idLibro")
    @Mapping(target = "titulo", source = "libro.titulo")
    @Mapping(target = "vendedor", source = "libro.vendedor")
    DetallePedidoResponse toDetalleResponse(DetallePedido detalle);

    @Mapping(target = "nombreCompleto",
            expression = "java(usuario.getNombres() + \" \" + usuario.getApellidos())")
    CompradorResumen toComprador(Usuario usuario);
}
