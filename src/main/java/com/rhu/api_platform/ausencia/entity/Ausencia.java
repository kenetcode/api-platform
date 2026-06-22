package com.rhu.api_platform.ausencia.entity;

import com.rhu.api_platform.empleado.entity.Empleado;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro unificado de ausencias e incapacidades.
 * Cubre todos los tipos definidos en la tabla legal de requerimientos.
 */
@Entity
@Table(name = "ausencias")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAusencia tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private Integer dias;

    @Column(precision = 5, scale = 2)
    private BigDecimal horas;

    @Column(name = "documento_respaldo_url", length = 500)
    private String documentoRespaldoUrl;

    @Column(name = "pago_porcentaje", precision = 5, scale = 2)
    private BigDecimal pagoPorcentaje;

    @Column(length = 500)
    private String observacion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime creadoEn;

    /**
     * Indica si este tipo de ausencia mantiene el goce de sueldo.
     */
    public boolean conGoceSueldo() {
        return this.tipo == TipoAusencia.PERMISO_CON_GOCE
                || this.tipo == TipoAusencia.INCAPACIDAD_ISSS_TOTAL;
    }

    /**
     * Indica si este tipo afecta/descuenta el séptimo día.
     */
    public boolean afectaSeptimoDia() {
        return this.tipo == TipoAusencia.FALTA_INJUSTIFICADA;
    }

    /**
     * Indica si es alguna modalidad de incapacidad.
     */
    public boolean esIncapacidad() {
        return this.tipo == TipoAusencia.INCAPACIDAD_COMUN
                || this.tipo == TipoAusencia.INCAPACIDAD_ISSS_TOTAL;
    }
}
