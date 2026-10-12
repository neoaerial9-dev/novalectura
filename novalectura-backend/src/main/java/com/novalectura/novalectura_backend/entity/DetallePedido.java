package com.novalectura.novalectura_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
@NoArgsConstructor
@Data
public class DetallePedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetallePedido;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;

    public DetallePedido(Pedido pedido, Libro libro, Integer cantidad) {
        this.pedido = pedido;
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
