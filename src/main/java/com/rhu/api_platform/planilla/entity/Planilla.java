package com.rhu.api_platform.planilla.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planillas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Planilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "periodo_mes", nullable = false, length = 7)
    private String periodoMes; // YYYY-MM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoPlanilla tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default
    private EstadoPlanilla estado = EstadoPlanilla.BORRADOR;

    @Column(name = "total_bruto", precision = 12, scale = 2)
    private BigDecimal totalBruto;

    @Column(name = "total_isss", precision = 10, scale = 2)
    private BigDecimal totalIsss;

    @Column(name = "total_afp", precision = 10, scale = 2)
    private BigDecimal totalAfp;

    @Column(name = "total_isr", precision = 10, scale = 2)
    private BigDecimal totalIsr;

    @Column(name = "total_neto", precision = 12, scale = 2)
    private BigDecimal totalNeto;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "aprobado_en")
    private LocalDateTime aprobadoEn;

    @OneToMany(mappedBy = "planilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetallePlanilla> detalles = new ArrayList<>();

    @PrePersist
    protected void onPersist() {
        creadoEn = LocalDateTime.now();
        if (estado == null) estado = EstadoPlanilla.BORRADOR;
    }
}
