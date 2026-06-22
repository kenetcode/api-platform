package com.rhu.api_platform.planilla.motor;

import com.rhu.api_platform.ausencia.entity.Ausencia;
import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import com.rhu.api_platform.empresa.ParametroEmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calcula el impacto de ausencias/incapacidades sobre un período de planilla.
 */
@Component
@RequiredArgsConstructor
public class CalculadoraAusencias {

    private final ParametroEmpresaService parametroEmpresaService;

    public ResumenAusencias calcular(List<Ausencia> ausencias, LocalDate inicioPeriodo, LocalDate finPeriodo) {
        ResumenAusencias.ResumenAusenciasBuilder builder = ResumenAusencias.builder();

        int diasDescontar = 0;
        int diasPagoParcial = 0;
        BigDecimal horasDescontar = BigDecimal.ZERO;
        int diasReportarIsssAfp = 0;

        boolean asume3Dias = parametroEmpresaService.asumePrimeros3DiasIncapacidad();
        BigDecimal porcentaje3Dias = parametroEmpresaService.porcentajePagoPrimeros3Dias();

        for (Ausencia ausencia : ausencias) {
            LocalDate inicio = max(ausencia.getFechaInicio(), inicioPeriodo);
            LocalDate fin = min(ausencia.getFechaFin(), finPeriodo);
            if (inicio.isAfter(fin)) {
                continue;
            }
            int diasEnPeriodo = (int) ChronoUnit.DAYS.between(inicio, fin) + 1;

            switch (ausencia.getTipo()) {
                case INCAPACIDAD_COMUN -> {
                    if (asume3Dias) {
                        int diasPrevios = diasAntesDelPeriodo(ausencia.getFechaInicio(), inicioPeriodo);
                        int diasCubiertosEmpresa = Math.max(0, 3 - diasPrevios);
                        int diasPagoParcialAusencia = Math.min(diasEnPeriodo, diasCubiertosEmpresa);
                        int diasDescuentoAusencia = diasEnPeriodo - diasPagoParcialAusencia;

                        diasPagoParcial += diasPagoParcialAusencia;
                        diasDescontar += diasDescuentoAusencia;
                    } else {
                        diasDescontar += diasEnPeriodo;
                    }
                }
                case INCAPACIDAD_ISSS_TOTAL -> {
                    diasDescontar += diasEnPeriodo;
                    diasReportarIsssAfp += diasEnPeriodo;
                }
                case PERMISO_CON_GOCE -> {
                    // No afecta el pago
                }
                case PERMISO_SIN_GOCE -> diasDescontar += diasEnPeriodo;
                case FALTA_INJUSTIFICADA -> diasDescontar += diasEnPeriodo;
                case AUSENCIA_POR_HORAS -> {
                    if (ausencia.getHoras() != null) {
                        horasDescontar = horasDescontar.add(ausencia.getHoras());
                    }
                }
            }
        }

        return builder
                .diasDescontar(diasDescontar)
                .diasPagoParcial(diasPagoParcial)
                .porcentajePagoParcial(porcentaje3Dias)
                .horasDescontar(horasDescontar)
                .diasReportarIsssAfp(diasReportarIsssAfp)
                .semanasConFaltaInjustificada(contarSemanasConFaltaInjustificada(ausencias, inicioPeriodo, finPeriodo))
                .build();
    }

    private int diasAntesDelPeriodo(LocalDate inicioAusencia, LocalDate inicioPeriodo) {
        if (!inicioAusencia.isBefore(inicioPeriodo)) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(inicioAusencia, inicioPeriodo);
    }

    private int contarSemanasConFaltaInjustificada(List<Ausencia> ausencias,
                                                   LocalDate inicioPeriodo,
                                                   LocalDate finPeriodo) {
        long semanas = ChronoUnit.WEEKS.between(inicioPeriodo, finPeriodo) + 1;
        int conteo = 0;
        for (int s = 0; s < semanas; s++) {
            LocalDate inicioSemana = inicioPeriodo.plusWeeks(s);
            LocalDate finSemana = inicioSemana.plusDays(6).isAfter(finPeriodo) ? finPeriodo : inicioSemana.plusDays(6);
            for (Ausencia ausencia : ausencias) {
                if (ausencia.getTipo() == TipoAusencia.FALTA_INJUSTIFICADA) {
                    LocalDate inicio = max(ausencia.getFechaInicio(), inicioSemana);
                    LocalDate fin = min(ausencia.getFechaFin(), finSemana);
                    if (!inicio.isAfter(fin)) {
                        conteo++;
                        break;
                    }
                }
            }
        }
        return conteo;
    }

    private LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
