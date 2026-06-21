package com.rhu.api_platform.planilla;

import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.Empleado;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import com.rhu.api_platform.planilla.dto.*;
import com.rhu.api_platform.planilla.entity.*;
import com.rhu.api_platform.planilla.motor.MotorCalculo;
import com.rhu.api_platform.planilla.motor.ParametrosCalculo;
import com.rhu.api_platform.planilla.motor.ResultadoCalculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanillaService {

    private final PlanillaRepository planillaRepository;
    private final DetallePlanillaRepository detallePlanillaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MotorCalculo motorCalculo;
    private final ParametroLegalService parametroLegalService;

    @Transactional
    public PlanillaResponse crearPlanilla(CrearPlanillaRequest req) {
        Planilla planilla = Planilla.builder()
                .periodoMes(req.getPeriodoMes())
                .tipo(req.getTipo())
                .estado(EstadoPlanilla.BORRADOR)
                .build();
        return toResponse(planillaRepository.save(planilla), false);
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
            ResultadoCalculo resultado = motorCalculo.calcular(
                    detalle.getEmpleado().getSalarioBase(),
                    detalle.getDiasLaborados(),
                    detalle.getHorasExtraDiurnas(),
                    detalle.getHorasExtraNocturnas(),
                    detalle.getComisiones(),
                    detalle.getBonificaciones(),
                    detalle.getDescuentosVoluntarios(),
                    params);

            detalle.setSalarioBruto(resultado.getSalarioBruto());
            detalle.setIsss(resultado.getIsss());
            detalle.setAfp(resultado.getAfp());
            detalle.setIsr(resultado.getIsr());
            detalle.setSalarioNeto(resultado.getSalarioNeto());
            detalle.setAportePatronalIsss(resultado.getAportePatronalIsss());
            detalle.setAportePatronalAfp(resultado.getAportePatronalAfp());
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
            int anios = Period.between(empleado.getFechaIngreso(), LocalDate.now()).getYears();
            monto = motorCalculo.calcularAguinaldo(empleado.getSalarioBase(), anios);
            int dias = anios < 1 ? 0 : anios < 3 ? 15 : anios < 10 ? 19 : 21;
            descripcion = "Aguinaldo: " + dias + " días (" + anios + " años de antigüedad)";
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
