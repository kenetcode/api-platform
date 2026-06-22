package com.rhu.api_platform.ausencia.entity;

/**
 * Tipos de ausencia/incapacidad según la normativa laboral salvadoreña
 * y la tabla de tipos definida en los requerimientos del sistema.
 */
public enum TipoAusencia {
    INCAPACIDAD_COMUN,          // Enfermedad o accidente común
    INCAPACIDAD_ISSS_TOTAL,     // Maternidad y riesgo profesional
    PERMISO_CON_GOCE,           // Paternidad, duelo, matrimonio, enfermedad grave de familiares
    PERMISO_SIN_GOCE,           // Permisos personales, emergencias no cubiertas por ley
    FALTA_INJUSTIFICADA,        // Faltas sin avisar, ausencias no autorizadas, suspensiones
    AUSENCIA_POR_HORAS          // Citas médicas ISSS, llegadas tardías, lactancia
}
