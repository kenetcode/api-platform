package com.rhu.api_platform.turno.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * Turno laboral. Define los días laborables, horario y horas ordinarias diarias.
 */
@Entity
@Table(name = "turnos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "dias_laborables", nullable = false, length = 100)
    @Convert(converter = DiasSemanaConverter.class)
    private Set<DiaSemana> diasLaborables;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "horas_ordinarias_diarias", nullable = false, precision = 4, scale = 2)
    private BigDecimal horasOrdinariasDiarias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EstadoTurno estado = EstadoTurno.ACTIVO;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    @UpdateTimestamp
    private LocalDateTime actualizadoEn;
}
