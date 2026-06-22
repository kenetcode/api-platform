package com.rhu.api_platform.empresa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parámetros configurables propios de la empresa.
 * Permiten ajustar comportamientos del motor de cálculo sin modificar código.
 */
@Entity
@Table(name = "parametros_empresa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoParametroEmpresa tipo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "vigencia")
    private LocalDate vigencia;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}
