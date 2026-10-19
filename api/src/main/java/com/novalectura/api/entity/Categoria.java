package com.novalectura.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
