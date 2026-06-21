package com.rhu.api_platform.empleado.dto;

import com.rhu.api_platform.empleado.entity.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CrearEmpleadoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DUI es obligatorio")
    @Pattern(regexp = "^\\d{8}-\\d$", message = "El DUI debe tener el formato ########-#")
    private String dui;

    @Pattern(regexp = "^\\d{4}-\\d{6}-\\d{3}-\\d$", message = "Formato de NIT inválido")
    private String nit;

    @Email(message = "Formato de correo inválido")
    private String correo;

    @Pattern(regexp = "^[0-9]{7,8}$", message = "El teléfono debe tener 7 u 8 dígitos")
    private String telefono;

    private LocalDate fechaNacimiento;
    private Genero genero;
    private String direccion;
    private String municipio;
    private String departamento;
    private String cargo;
    private String departamentoLab;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    private TipoContrato tipoContrato;

    @NotNull(message = "El salario base es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero")
    private BigDecimal salarioBase;

    @NotNull(message = "El sector es obligatorio")
    private SectorEmpleado sector;

    @NotBlank(message = "La AFP es obligatoria")
    private String afp;

    private String numIsss;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private Boolean esBorrador;
}
