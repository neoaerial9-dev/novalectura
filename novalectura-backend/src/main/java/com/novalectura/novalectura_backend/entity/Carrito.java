package com.novalectura.novalectura_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@NoArgsConstructor
@Data
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCarrito> detalles = new ArrayList<>();

    @PrePersist
    void alCrear() {
        fechaCreacion = LocalDateTime.now();
    }

    public void agregarLibro(Libro libro, Integer cantidad) {
        validarCantidad(cantidad);
        detalles.stream()
                .filter(d -> d.getLibro().getIdLibro().equals(libro.getIdLibro()))
                .findFirst()
                .ifPresentOrElse(
                        d -> d.setCantidad(d.getCantidad() + cantidad),
                        () -> detalles.add(new DetalleCarrito(this, libro, cantidad)));
        calcularTotal();
    }

    public boolean eliminarLibro(Long idDetalle) {
        boolean eliminado = detalles.removeIf(d -> idDetalle.equals(d.getIdDetalle()));
        calcularTotal();
        return eliminado;
    }

    public boolean actualizarCantidad(Long idDetalle, Integer cantidad) {
        validarCantidad(cantidad);
        boolean actualizado = detalles.stream()
                .filter(d -> idDetalle.equals(d.getIdDetalle()))
                .findFirst()
                .map(d -> {
                    d.setCantidad(cantidad);
                    return true;
                })
                .orElse(false);
        calcularTotal();
        return actualizado;
    }

    public BigDecimal calcularTotal() {
        total = detalles.stream()
                .map(DetalleCarrito::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    public void vaciarCarrito() {
        detalles.clear();
        total = BigDecimal.ZERO;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }
}
