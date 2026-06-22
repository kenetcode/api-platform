package com.rhu.api_platform.planilla;

import com.rhu.api_platform.ausencia.AusenciaRepository;
import com.rhu.api_platform.ausencia.entity.Ausencia;
import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.Empleado;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import com.rhu.api_platform.planilla.dto.*;
import com.rhu.api_platform.planilla.entity.*;
import com.rhu.api_platform.planilla.motor.CalculadoraAusencias;
import com.rhu.api_platform.planilla.motor.CalculadoraQuincena25;
import com.rhu.api_platform.planilla.motor.MotorCalculo;
import com.rhu.api_platform.planilla.motor.ParametrosCalculo;
import com.rhu.api_platform.planilla.motor.ResumenAusencias;
import com.rhu.api_platform.planilla.motor.ResultadoCalculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanillaService {

    private final PlanillaRepository planillaRepository;
    private final DetallePlanillaRepository detallePlanillaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AusenciaRepository ausenciaRepository;
    private final MotorCalculo motorCalculo;
    private final CalculadoraAusencias calculadoraAusencias;
    private final CalculadoraQuincena25 calculadoraQuincena25;
    private final ParametroLegalService parametroLegalService;

    @Transactional
    public PlanillaResponse crearPlanilla(CrearPlanillaRequest req) {
        PeriodoPlanilla periodo = resolverPeriodo(req);

        if (req.getTipo() == TipoPlanilla.QUINCENA_25
                && planillaRepository.existsByTipoAndPeriodoMes(TipoPlanilla.QUINCENA_25, req.getPeriodoMes())) {
            throw new ConflictoException("Ya existe una planilla de Quincena 25 para el período " + req.getPeriodoMes());
        }

        Planilla planilla = Planilla.builder()
                .periodoMes(req.getPeriodoMes())
                .numeroQuincena(req.getNumeroQuincena())
                .fechaInicio(periodo.fechaInicio())
                .fechaFin(periodo.fechaFin())
                .tipo(req.getTipo())
                .estado(EstadoPlanilla.BORRADOR)
                .build();
        return toResponse(planillaRepository.save(planilla), false);
    }

    private record PeriodoPlanilla(LocalDate fechaInicio, LocalDate fechaFin) {}

    private PeriodoPlanilla resolverPeriodo(CrearPlanillaRequest req) {
        YearMonth ym = YearMonth.parse(req.getPeriodoMes());
        LocalDate inicio;
        LocalDate fin;

        if (req.getTipo() == TipoPlanilla.QUINCENA_25) {
            if (req.getNumeroQuincena() != null) {
                throw new ValidacionNegocioException("Para Quincena 25 no debe indicar número de quincena.");
            }
            if (ym.getMonthValue() != 1) {
                throw new ValidacionNegocioException("La Quincena 25 debe crearse en el período de enero (YYYY-01).");
            }
            inicio = ym.atDay(15);
            fin = ym.atDay(25);
        } else if (req.getTipo() == TipoPlanilla.QUINCENAL) {
            if (req.getNumeroQuincena() == null) {
                throw new ValidacionNegocioException("Para planilla quincenal debe indicar el número de quincena (1 o 2).");
            }
            if (req.getNumeroQuincena() != 1 && req.getNumeroQuincena() != 2) {
                throw new ValidacionNegocioException("El número de quincena debe ser 1 o 2.");
            }
            if (req.getNumeroQuincena() == 1) {
                inicio = ym.atDay(1);
                fin = ym.atDay(15);
            } else {
                inicio = ym.atDay(16);
                fin = ym.atEndOfMonth();
            }
        } else {
            throw new ValidacionNegocioException("Tipo de planilla no válido. Use: QUINCENAL o QUINCENA_25.");
        }

        // Si el usuario envía fechas, se validan contra el período esperado
        if (req.getFechaInicio() != null && !req.getFechaInicio().equals(inicio)) {
            throw new ValidacionNegocioException(
                    "La fecha de inicio debe coincidir con el período. Esperado: " + inicio);
        }
        if (req.getFechaFin() != null && !req.getFechaFin().equals(fin)) {
            throw new ValidacionNegocioException(
                    "La fecha de fin debe coincidir con el período. Esperado: " + fin);
        }

        return new PeriodoPlanilla(inicio, fin);
    }

    @Transactional
    public DetallePlanillaResponse capturarDetalle(Long planillaId, CapturarDetalleRequest req) {
        Planilla planilla = obtenerPlanilla(planillaId);
        if (planilla.getEstado() == EstadoPlanilla.APROBADA) {
            throw new ValidacionNegocioException("No se puede modificar una planilla aprobada.");
        }
        Empleado empleado = empleadoRepository.findById(req.getEmpleadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + req.getEmpleadoId()));
        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new ValidacionNegocioException("El empleado no está activo.");
        }

        DetallePlanilla detalle = detallePlanillaRepository
                .findByPlanillaIdAndEmpleadoId(planillaId, req.getEmpleadoId())
                .orElse(DetallePlanilla.builder().planilla(planilla).empleado(empleado).build());

        detalle.setDiasLaborados(req.getDiasLaborados());
        detalle.setHorasExtraDiurnas(req.getHorasExtraDiurnas() != null ? req.getHorasExtraDiurnas() : BigDecimal.ZERO);
        detalle.setHorasExtraNocturnas(req.getHorasExtraNocturnas() != null ? req.getHorasExtraNocturnas() : BigDecimal.ZERO);
        detalle.setComisiones(req.getComisiones() != null ? req.getComisiones() : BigDecimal.ZERO);
        detalle.setBonificaciones(req.getBonificaciones() != null ? req.getBonificaciones() : BigDecimal.ZERO);
        detalle.setDescuentosVoluntarios(req.getDescuentosVoluntarios() != null ? req.getDescuentosVoluntarios() : BigDecimal.ZERO);
        detalle.setDiasDescansoTrabajados(req.getDiasDescansoTrabajados());

        return toDetalleResponse(detallePlanillaRepository.save(detalle));
    }

    @Transactional
    public PlanillaResponse calcular(Long planillaId) {
        Planilla planilla = obtenerPlanilla(planillaId);
        if (planilla.getEstado() == EstadoPlanilla.APROBADA) {
            throw new ValidacionNegocioException("La planilla ya está aprobada.");
        }

        ParametrosCalculo params = parametroLegalService.cargarParametros();
        List<DetallePlanilla> detalles = detallePlanillaRepository.findByPlanillaId(planillaId);

        if (detalles.isEmpty()) {
            throw new ValidacionNegocioException("La planilla no tiene detalles para calcular.");
        }

        BigDecimal totalBruto = BigDecimal.ZERO;
        BigDecimal totalIsss = BigDecimal.ZERO;
        BigDecimal totalAfp = BigDecimal.ZERO;
        BigDecimal totalIsr = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (DetallePlanilla detalle : detalles) {
            ResultadoCalculo resultado = planilla.getTipo() == TipoPlanilla.QUINCENA_25
                    ? calcularQuincena25(detalle, params)
                    : calcularOrdinario(detalle, planilla, params);

            aplicarResultado(detalle, resultado);
            detallePlanillaRepository.save(detalle);

            totalBruto = totalBruto.add(resultado.getSalarioBruto());
            totalIsss = totalIsss.add(resultado.getIsss());
            totalAfp = totalAfp.add(resultado.getAfp());
            totalIsr = totalIsr.add(resultado.getIsr());
            totalNeto = totalNeto.add(resultado.getSalarioNeto());
        }

        planilla.setTotalBruto(totalBruto);
        planilla.setTotalIsss(totalIsss);
        planilla.setTotalAfp(totalAfp);
        planilla.setTotalIsr(totalIsr);
        planilla.setTotalNeto(totalNeto);
        planilla.setEstado(EstadoPlanilla.CALCULADA);
        return toResponse(planillaRepository.save(planilla), true);
    }

    private ResultadoCalculo calcularOrdinario(DetallePlanilla detalle, Planilla planilla, ParametrosCalculo params) {
        List<Ausencia> ausencias = ausenciaRepository.findSolapadas(
                detalle.getEmpleado().getId(),
                planilla.getFechaInicio(),
                planilla.getFechaFin());
        ResumenAusencias resumenAusencias = calculadoraAusencias.calcular(
                ausencias, planilla.getFechaInicio(), planilla.getFechaFin());

        return motorCalculo.calcular(
                detalle.getEmpleado().getSalarioBase(),
                detalle.getDiasLaborados(),
                detalle.getHorasExtraDiurnas(),
                detalle.getHorasExtraNocturnas(),
                detalle.getComisiones(),
                detalle.getBonificaciones(),
                detalle.getDescuentosVoluntarios(),
                params,
                resumenAusencias,
                detalle.getDiasDescansoTrabajados() != null ? detalle.getDiasDescansoTrabajados() : 0);
    }

    private ResultadoCalculo calcularQuincena25(DetallePlanilla detalle, ParametrosCalculo params) {
        YearMonth ym = YearMonth.parse(detalle.getPlanilla().getPeriodoMes());
        LocalDate fechaCalculo = ym.atDay(25);
        return calculadoraQuincena25.calcular(detalle.getEmpleado(), fechaCalculo, params);
    }

    private void aplicarResultado(DetallePlanilla detalle, ResultadoCalculo resultado) {
        detalle.setSalarioBruto(resultado.getSalarioBruto());
        detalle.setIsss(resultado.getIsss());
        detalle.setAfp(resultado.getAfp());
        detalle.setIsr(resultado.getIsr());
        detalle.setSalarioNeto(resultado.getSalarioNeto());
        detalle.setAportePatronalIsss(resultado.getAportePatronalIsss());
        detalle.setAportePatronalAfp(resultado.getAportePatronalAfp());
        detalle.setDiasDescontados(resultado.getDiasDescontados());
        detalle.setDiasPagoParcial(resultado.getDiasPagoParcial());
        detalle.setPorcentajePagoParcial(resultado.getPorcentajePagoParcial());
        detalle.setHorasDescontadas(resultado.getHorasDescontadas());
        detalle.setDiasReportarIsssAfp(resultado.getDiasReportarIsssAfp());
        detalle.setRecargoDescansoTrabajado(resultado.getRecargoDescansoTrabajado());
        detalle.setDescuentoSeptimoDia(resultado.getDescuentoSeptimoDia());
        detalle.setSemanasConFaltaInjustificada(resultado.getSemanasConFaltaInjustificada());
        detalle.setMontoQuincena25(resultado.getMontoQuincena25());
    }

    @Transactional
    public PlanillaResponse aprobar(Long planillaId) {
        Planilla planilla = obtenerPlanilla(planillaId);
        if (planilla.getEstado() != EstadoPlanilla.CALCULADA) {
            throw new ValidacionNegocioException("La planilla debe estar en estado CALCULADA para aprobar.");
        }
        planilla.setEstado(EstadoPlanilla.APROBADA);
        planilla.setAprobadoEn(LocalDateTime.now());
        return toResponse(planillaRepository.save(planilla), true);
    }

    public PlanillaResponse obtener(Long planillaId) {
        return toResponse(obtenerPlanilla(planillaId), true);
    }

    public List<PlanillaResponse> listar() {
        return planillaRepository.findAllByOrderByCreadoEnDesc()
                .stream().map(p -> toResponse(p, false)).toList();
    }

    public PrestacionesResponse calcularPrestaciones(Long empleadoId, String tipo) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + empleadoId));

        BigDecimal monto;
        String descripcion;

        if ("vacaciones".equalsIgnoreCase(tipo)) {
            monto = motorCalculo.calcularVacaciones(empleado.getSalarioBase());
            descripcion = "15 días de salario + 30% de prima vacacional (Art. 177 CT)";
        } else if ("aguinaldo".equalsIgnoreCase(tipo)) {
            LocalDate fechaCalculo = LocalDate.now();
            monto = motorCalculo.calcularAguinaldo(empleado.getSalarioBase(), empleado.getFechaIngreso(), fechaCalculo);
            int anios = Period.between(empleado.getFechaIngreso(), fechaCalculo).getYears();
            String diasTexto = anios < 1 ? "proporcional" : anios < 3 ? "15" : anios < 10 ? "19" : "21";
            descripcion = "Aguinaldo: " + diasTexto + " días (" + anios + " años de antigüedad)";
        } else {
            throw new ValidacionNegocioException("Tipo de prestación no válido. Use: vacaciones o aguinaldo");
        }

        return PrestacionesResponse.builder()
                .empleadoId(empleadoId)
                .nombreEmpleado(empleado.getNombre() + " " + empleado.getApellido())
                .tipo(tipo)
                .monto(monto)
                .descripcion(descripcion)
                .build();
    }

    private Planilla obtenerPlanilla(Long id) {
        return planillaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Planilla no encontrada: " + id));
    }

    private PlanillaResponse toResponse(Planilla p, boolean incluirDetalles) {
        PlanillaResponse.PlanillaResponseBuilder builder = PlanillaResponse.builder()
                .id(p.getId())
                .periodoMes(p.getPeriodoMes())
                .numeroQuincena(p.getNumeroQuincena())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .tipo(p.getTipo())
                .estado(p.getEstado())
                .totalBruto(p.getTotalBruto())
                .totalIsss(p.getTotalIsss())
                .totalAfp(p.getTotalAfp())
                .totalIsr(p.getTotalIsr())
                .totalNeto(p.getTotalNeto())
                .creadoEn(p.getCreadoEn())
                .aprobadoEn(p.getAprobadoEn());

        if (incluirDetalles) {
            List<DetallePlanilla> detalles = detallePlanillaRepository.findByPlanillaId(p.getId());
            builder.detalles(detalles.stream().map(this::toDetalleResponse).toList());
        }
        return builder.build();
    }

    private DetallePlanillaResponse toDetalleResponse(DetallePlanilla d) {
        return DetallePlanillaResponse.builder()
                .id(d.getId())
                .empleadoId(d.getEmpleado().getId())
                .nombreEmpleado(d.getEmpleado().getNombre() + " " + d.getEmpleado().getApellido())
                .diasLaborados(d.getDiasLaborados())
                .horasExtraDiurnas(d.getHorasExtraDiurnas())
                .horasExtraNocturnas(d.getHorasExtraNocturnas())
                .comisiones(d.getComisiones())
                .bonificaciones(d.getBonificaciones())
                .descuentosVoluntarios(d.getDescuentosVoluntarios())
                .diasDescontados(d.getDiasDescontados())
                .diasPagoParcial(d.getDiasPagoParcial())
                .porcentajePagoParcial(d.getPorcentajePagoParcial())
                .horasDescontadas(d.getHorasDescontadas())
                .diasReportarIsssAfp(d.getDiasReportarIsssAfp())
                .diasDescansoTrabajados(d.getDiasDescansoTrabajados())
                .recargoDescansoTrabajado(d.getRecargoDescansoTrabajado())
                .semanasConFaltaInjustificada(d.getSemanasConFaltaInjustificada())
                .descuentoSeptimoDia(d.getDescuentoSeptimoDia())
                .montoQuincena25(d.getMontoQuincena25())
                .salarioBruto(d.getSalarioBruto())
                .isss(d.getIsss())
                .afp(d.getAfp())
                .isr(d.getIsr())
                .salarioNeto(d.getSalarioNeto())
                .aportePatronalIsss(d.getAportePatronalIsss())
                .aportePatronalAfp(d.getAportePatronalAfp())
                .build();
    }
}
