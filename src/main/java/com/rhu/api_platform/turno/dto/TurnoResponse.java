package com.rhu.api_platform.turno.dto;

import com.rhu.api_platform.turno.entity.DiaSemana;
import com.rhu.api_platform.turno.entity.EstadoTurno;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
public class TurnoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Set<DiaSemana> diasLaborables;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private BigDecimal horasOrdinariasDiarias;
    private EstadoTurno estado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
