package com.rhu.api_platform.ausencia.entity;

import com.rhu.api_platform.empleado.entity.Empleado;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "incapacidades")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incapacidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoIncapacidad tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Integer dias;

    @Column(name = "documento_url", length = 500)
    private String documentoUrl;
}
