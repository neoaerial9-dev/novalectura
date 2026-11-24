package com.novalectura.api.service;

public enum RecursoCambiado {
    CATALOGO("catalogo"),
    PEDIDOS("pedidos"),
    USUARIOS("usuarios");

    private final String evento;

    RecursoCambiado(String evento) {
        this.evento = evento;
    }

    public String evento() {
        return evento;
    }
}
