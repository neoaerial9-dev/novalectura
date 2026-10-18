package com.novalectura.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "libros")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long idLibro;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String autor;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String imagen;

    @Column(name = "estado_conservacion", nullable = false, length = 50)
    private String estadoConservacion;

    @Column(name = "detalles_uso", columnDefinition = "TEXT")
    private String detallesUso;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
}
