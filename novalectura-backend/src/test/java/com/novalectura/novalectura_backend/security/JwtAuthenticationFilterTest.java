package com.novalectura.novalectura_backend.security;

import com.novalectura.novalectura_backend.testutil.Datos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {
    @Mock JwtService jwtService;
    @Mock UsuarioDetailsService usuarioDetailsService;

    private JwtAuthenticationFilter filtro;
    private MockHttpServletRequest peticion;
    private MockHttpServletResponse respuesta;
    private MockFilterChain cadena;

    @BeforeEach
    void iniciar() {
        SecurityContextHolder.clearContext();
        filtro = new JwtAuthenticationFilter(jwtService, usuarioDetailsService);
        peticion = new MockHttpServletRequest();
        respuesta = new MockHttpServletResponse();
        cadena = new MockFilterChain();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("con un token válido autentica la petición con el usuario de la base de datos")
    void tokenValido() throws Exception {
        UsuarioDetails detalles = new UsuarioDetails(Datos.usuario(1L));
        peticion.addHeader("Authorization", "Bearer token-bueno");
        when(jwtService.extraerCorreo("token-bueno")).thenReturn(Optional.of("user1@mail.com"));
        when(usuarioDetailsService.loadUserByUsername("user1@mail.com")).thenReturn(detalles);

        filtro.doFilter(peticion, respuesta, cadena);

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        assertThat(autenticacion).isNotNull();
        assertThat(autenticacion.isAuthenticated()).isTrue();
        assertThat(autenticacion.getPrincipal()).isSameAs(detalles);
        assertThat(cadena.getRequest()).as("la petición continúa por la cadena").isNotNull();
    }

    @Test
    @DisplayName("sin cabecera Authorization no autentica y deja pasar la petición")
    void sinCabecera() throws Exception {
        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadena.getRequest()).isNotNull();
        verifyNoInteractions(jwtService, usuarioDetailsService);
    }

    @Test
    @DisplayName("una cabecera que no es Bearer (por ejemplo Basic) se ignora")
    void esquemaDistinto() throws Exception {
        peticion.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadena.getRequest()).isNotNull();
        verifyNoInteractions(jwtService, usuarioDetailsService);
    }

    @Test
    @DisplayName("un token inválido o expirado no autentica (las reglas de acceso responderán 401)")
    void tokenInvalido() throws Exception {
        peticion.addHeader("Authorization", "Bearer token-malo");
        when(jwtService.extraerCorreo("token-malo")).thenReturn(Optional.empty());

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadena.getRequest()).isNotNull();
        verifyNoInteractions(usuarioDetailsService);
    }

    @Test
    @DisplayName("un token válido de un usuario que ya no existe no autentica")
    void usuarioInexistente() throws Exception {
        peticion.addHeader("Authorization", "Bearer token-bueno");
        when(jwtService.extraerCorreo("token-bueno")).thenReturn(Optional.of("fantasma@mail.com"));
        when(usuarioDetailsService.loadUserByUsername("fantasma@mail.com"))
                .thenThrow(new UsernameNotFoundException("no existe"));

        filtro.doFilter(peticion, respuesta, cadena);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(cadena.getRequest()).isNotNull();
    }
}
