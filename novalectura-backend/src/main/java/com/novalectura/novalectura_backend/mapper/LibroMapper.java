package com.novalectura.novalectura_backend.mapper;

import com.novalectura.novalectura_backend.dto.request.LibroRequest;
import com.novalectura.novalectura_backend.dto.response.LibroResponse;
import com.novalectura.novalectura_backend.dto.response.VendedorResumen;
import com.novalectura.novalectura_backend.entity.Libro;
import com.novalectura.novalectura_backend.entity.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CategoriaMapper.class)
public interface LibroMapper {
    LibroResponse toResponse(Libro libro);

    @Mapping(target = "nombreCompleto", expression = "java(usuario.getNombres() + \" \" + usuario.getApellidos())")
    VendedorResumen toVendedor(Usuario usuario);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "precio", source = "precio")
    @Mapping(target = "stock", source = "stock")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "imagen", source = "imagen")
    @Mapping(target = "condicion", source = "condicion")
    Libro toEntity(LibroRequest request);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "titulo", source = "titulo")
    @Mapping(target = "autor", source = "autor")
    @Mapping(target = "precio", source = "precio")
    @Mapping(target = "stock", source = "stock")
    @Mapping(target = "descripcion", source = "descripcion")
    @Mapping(target = "imagen", source = "imagen")
    @Mapping(target = "condicion", source = "condicion")
    void actualizar(LibroRequest request, @MappingTarget Libro libro);
}
