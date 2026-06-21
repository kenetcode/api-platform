package com.rhu.api_platform.planilla.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "parametros_legales")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroLegal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String clave;

    @Column(nullable = false, length = 50)
    private String valor;

    @Column(length = 200)
    private String descripcion;

    @Column
    private LocalDate vigencia;
}
