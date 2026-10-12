package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.entity.enums.EstadoPedido;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@NoArgsConstructor
@Data
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    void alCrear() {
        fechaPedido = LocalDateTime.now();
    }

    public static Pedido registrarPedido(Carrito carrito) {
        if (carrito.getDetalles().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }
        Pedido pedido = new Pedido();
        pedido.usuario = carrito.getUsuario();
        carrito.getDetalles().forEach(d ->
                pedido.detalles.add(new DetallePedido(pedido, d.getLibro(), d.getCantidad())));
        pedido.calcularTotal();
        return pedido;
    }

    public BigDecimal calcularTotal() {
        total = detalles.stream()
                .map(DetallePedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    public void actualizarEstado(EstadoPedido nuevoEstado) {
        this.estado = nuevoEstado;
    }
}
