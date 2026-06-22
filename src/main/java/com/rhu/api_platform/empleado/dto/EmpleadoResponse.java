package com.rhu.api_platform.empleado.dto;

import com.rhu.api_platform.empleado.entity.*;
import com.rhu.api_platform.turno.entity.DiaSemana;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
public class EmpleadoResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dui;
    private String nit;
    private String correo;
    private String telefono;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private String direccion;
    private String municipio;
    private String departamento;
    private String cargo;
    private String departamentoLab;
    private LocalDate fechaIngreso;
    private TipoContrato tipoContrato;
    private BigDecimal salarioBase;
    private SectorEmpleado sector;
    private String afp;
    private String numIsss;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private EstadoEmpleado estado;
    private Boolean esBorrador;
    private TurnoInfo turno;
    private LocalDateTime creadoEn;

    @Data
    @Builder
    public static class TurnoInfo {
        private Long id;
        private String nombre;
        private Set<DiaSemana> diasLaborables;
        private LocalTime horaEntrada;
        private LocalTime horaSalida;
        private BigDecimal horasOrdinariasDiarias;
    }
}
