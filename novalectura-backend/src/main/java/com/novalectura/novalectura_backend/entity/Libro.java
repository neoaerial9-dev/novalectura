package com.novalectura.novalectura_backend.entity;

import com.novalectura.novalectura_backend.entity.enums.CondicionLibro;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "libros")
@NoArgsConstructor
@Data
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLibro;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock = 1;

    @Column(length = 2000)
    private String descripcion;

    @Column(length = 500)
    private String imagen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CondicionLibro condicion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor;

    @PrePersist
    void alCrear() {
        fechaPublicacion = LocalDateTime.now();
    }

    public boolean estaDisponible() {
        return activo && stock > 0;
    }

    public void descontarStock(int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (stock < cantidad) {
            throw new IllegalStateException("Stock insuficiente para: " + titulo);
        }
        stock -= cantidad;
    }
}
