package com.novalectura.novalectura_backend.mapper;

import com.novalectura.novalectura_backend.dto.request.UsuarioPerfilRequest;
import com.novalectura.novalectura_backend.dto.response.UsuarioResponse;
import com.novalectura.novalectura_backend.entity.Usuario;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponse toResponse(Usuario usuario);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "nombres", source = "nombres")
    @Mapping(target = "apellidos", source = "apellidos")
    @Mapping(target = "telefono", source = "telefono")
    @Mapping(target = "direccion", source = "direccion")
    void actualizarPerfil(UsuarioPerfilRequest request, @MappingTarget Usuario usuario);
}
