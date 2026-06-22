package com.rhu.api_platform.turno.dto;

import com.rhu.api_platform.turno.entity.DiaSemana;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Set;

@Data
public class TurnoRequest {

    @NotBlank(message = "El nombre del turno es obligatorio")
    private String nombre;

    private String descripcion;

    @NotEmpty(message = "Debe indicar al menos un día laborable")
    private Set<DiaSemana> diasLaborables;

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime horaSalida;

    @NotNull(message = "Las horas ordinarias diarias son obligatorias")
    private BigDecimal horasOrdinariasDiarias;
}
