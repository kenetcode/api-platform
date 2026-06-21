package com.rhu.api_platform.empleado.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "empleados")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, unique = true, length = 10)
    private String dui;

    @Column(length = 20)
    private String nit;

    @Column(length = 150)
    private String correo;

    @Column(length = 15)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Genero genero;

    @Column(length = 300)
    private String direccion;

    @Column(length = 100)
    private String municipio;

    @Column(length = 100)
    private String departamento;

    @Column(length = 150)
    private String cargo;

    @Column(name = "departamento_lab", length = 100)
    private String departamentoLab;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", length = 20)
    private TipoContrato tipoContrato;

    @Column(name = "salario_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal salarioBase;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SectorEmpleado sector;

    @Column(length = 50)
    private String afp;

    @Column(name = "num_isss", length = 20)
    private String numIsss;

    @Column(name = "contacto_emergencia_nombre", length = 150)
    private String contactoEmergenciaNombre;

    @Column(name = "contacto_emergencia_telefono", length = 15)
    private String contactoEmergenciaTelefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EstadoEmpleado estado = EstadoEmpleado.ACTIVO;

    @Column(name = "es_borrador", nullable = false)
    @Builder.Default
    private Boolean esBorrador = false;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    protected void onPersist() {
        creadoEn = LocalDateTime.now();
        if (estado == null) estado = EstadoEmpleado.ACTIVO;
        if (esBorrador == null) esBorrador = false;
    }
}
