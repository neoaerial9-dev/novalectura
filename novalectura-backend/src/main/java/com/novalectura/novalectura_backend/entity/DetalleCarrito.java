package com.novalectura.novalectura_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_carrito", uniqueConstraints = @UniqueConstraint(columnNames = {"id_carrito", "id_libro"}))
@NoArgsConstructor
@Data
public class DetalleCarrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_carrito", nullable = false)
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    public DetalleCarrito(Carrito carrito, Libro libro, Integer cantidad) {
        this.carrito = carrito;
        this.libro = libro;
        this.cantidad = cantidad;
        this.precioUnitario = libro.getPrecio();
        calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() {
        subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        return subtotal;
    }
}
