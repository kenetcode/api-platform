package com.rhu.api_platform.planilla.boleta;

import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empresa.EmpresaRepository;
import com.rhu.api_platform.empresa.entity.Empresa;
import com.rhu.api_platform.planilla.DetallePlanillaRepository;
import com.rhu.api_platform.planilla.PlanillaRepository;
import com.rhu.api_platform.planilla.dto.DetallePlanillaResponse;
import com.rhu.api_platform.planilla.dto.PlanillaResponse;
import com.rhu.api_platform.planilla.entity.DetallePlanilla;
import com.rhu.api_platform.planilla.entity.Planilla;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoletaService {

    private final PlanillaRepository planillaRepository;
    private final DetallePlanillaRepository detallePlanillaRepository;
    private final EmpresaRepository empresaRepository;
    private final GeneradorPdf generadorPdf;

    public BoletaResponse obtenerBoletaJson(Long planillaId, Long empleadoId) {
        DetallePlanilla detalle = obtenerDetalle(planillaId, empleadoId);
        return construirBoleta(detalle);
    }

    public byte[] generarBoletaPdf(Long planillaId, Long empleadoId) {
        DetallePlanilla detalle = obtenerDetalle(planillaId, empleadoId);
        BoletaResponse boleta = construirBoleta(detalle);
        return generadorPdf.generarBoletaIndividual(boleta);
    }

    public byte[] exportarPlanillaPdf(Long planillaId) {
        Planilla planilla = planillaRepository.findById(planillaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Planilla no encontrada: " + planillaId));
        List<DetallePlanilla> detalles = detallePlanillaRepository.findByPlanillaId(planillaId);
        if (detalles.isEmpty()) {
            throw new ValidacionNegocioException("La planilla no tiene detalles para exportar.");
        }
        List<DetallePlanillaResponse> detalleResponses = detalles.stream()
                .map(d -> DetallePlanillaResponse.builder()
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
                        .salarioBruto(d.getSalarioBruto())
                        .isss(d.getIsss())
                        .afp(d.getAfp())
                        .isr(d.getIsr())
                        .salarioNeto(d.getSalarioNeto())
                        .aportePatronalIsss(d.getAportePatronalIsss())
                        .aportePatronalAfp(d.getAportePatronalAfp())
                        .build())
                .toList();
        PlanillaResponse planillaResponse = PlanillaResponse.builder()
                .id(planilla.getId())
                .periodoMes(planilla.getPeriodoMes())
                .numeroQuincena(planilla.getNumeroQuincena())
                .fechaInicio(planilla.getFechaInicio())
                .fechaFin(planilla.getFechaFin())
                .tipo(planilla.getTipo())
                .estado(planilla.getEstado())
                .totalBruto(planilla.getTotalBruto())
                .totalIsss(planilla.getTotalIsss())
                .totalAfp(planilla.getTotalAfp())
                .totalIsr(planilla.getTotalIsr())
                .totalNeto(planilla.getTotalNeto())
                .creadoEn(planilla.getCreadoEn())
                .detalles(detalleResponses)
                .build();
        String nombreEmpresa = empresaRepository.findAll().stream()
                .findFirst().map(Empresa::getNombre)
                .orElse("Supermercado La Cesta, S.A. de C.V.");
        return generadorPdf.generarPlanillaCompleta(planillaResponse, nombreEmpresa);
    }

    private DetallePlanilla obtenerDetalle(Long planillaId, Long empleadoId) {
        return detallePlanillaRepository.findByPlanillaIdAndEmpleadoId(planillaId, empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró detalle para planilla " + planillaId + " y empleado " + empleadoId));
    }

    private BoletaResponse construirBoleta(DetallePlanilla d) {
        BigDecimal isss = nvl(d.getIsss());
        BigDecimal afp = nvl(d.getAfp());
        BigDecimal isr = nvl(d.getIsr());
        BigDecimal descVol = nvl(d.getDescuentosVoluntarios());
        BigDecimal aporteIsss = nvl(d.getAportePatronalIsss());
        BigDecimal aporteAfp = nvl(d.getAportePatronalAfp());
        String nombreEmpresa = empresaRepository.findAll().stream()
                .findFirst().map(Empresa::getNombre)
                .orElse("Supermercado La Cesta, S.A. de C.V.");
        return BoletaResponse.builder()
                .empresa(nombreEmpresa)
                .periodoMes(d.getPlanilla().getPeriodoMes())
                .tipoPlanilla(d.getPlanilla().getTipo().name())
                .fechaGeneracion(LocalDateTime.now())
                .empleadoId(d.getEmpleado().getId())
                .nombreCompleto(d.getEmpleado().getNombre() + " " + d.getEmpleado().getApellido())
                .dui(d.getEmpleado().getDui())
                .cargo(d.getEmpleado().getCargo())
                .departamento(d.getEmpleado().getDepartamentoLab())
                .afp(d.getEmpleado().getAfp())
                .diasLaborados(d.getDiasLaborados())
                .salarioBase(d.getEmpleado().getSalarioBase())
                .salarioProporcional(nvl(d.getSalarioBruto()))
                .horasExtraDiurnas(nvl(d.getHorasExtraDiurnas()))
                .horasExtraNocturnas(nvl(d.getHorasExtraNocturnas()))
                .comisiones(nvl(d.getComisiones()))
                .bonificaciones(nvl(d.getBonificaciones()))
                .totalPercepciones(nvl(d.getSalarioBruto()))
                .isss(isss)
                .afpMonto(afp)
                .isr(isr)
                .descuentosVoluntarios(descVol)
                .totalDeducciones(isss.add(afp).add(isr).add(descVol))
                .salarioNeto(nvl(d.getSalarioNeto()))
                .aportePatronalIsss(aporteIsss)
                .aportePatronalAfp(aporteAfp)
                .totalAportePatronal(aporteIsss.add(aporteAfp))
                .build();
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
