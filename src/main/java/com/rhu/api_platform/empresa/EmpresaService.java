package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.dto.DashboardResponse;
import com.rhu.api_platform.empresa.dto.EmpresaResponse;
import com.rhu.api_platform.empresa.entity.Empresa;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import com.rhu.api_platform.planilla.PlanillaRepository;
import com.rhu.api_platform.planilla.entity.EstadoPlanilla;
import com.rhu.api_platform.planilla.entity.Planilla;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final PlanillaRepository planillaRepository;

    public EmpresaResponse obtenerEmpresa() {
        Optional<Empresa> empresaOpt = empresaRepository.findAll().stream().findFirst();
        Empresa empresa = empresaOpt.orElseGet(() -> Empresa.builder()
                .nombre("Supermercado La Cesta, S.A. de C.V.")
                .logoUrl("/assets/logo.png")
                .direccion("San Salvador, El Salvador")
                .nit("0614-000000-000-0")
                .build());

        return EmpresaResponse.builder()
                .id(empresa.getId())
                .nombre(empresa.getNombre())
                .logoUrl(empresa.getLogoUrl())
                .direccion(empresa.getDireccion())
                .nit(empresa.getNit())
                .menu(EmpresaResponse.MenuInfo.builder()
                        .titulo("Sistema de Planillas")
                        .version("1.0.0")
                        .modulos(new String[]{"empleados", "ausencias", "planillas", "usuarios"})
                        .build())
                .build();
    }

    public DashboardResponse obtenerDashboard() {
        long activos = empleadoRepository.countByEstado(EstadoEmpleado.ACTIVO);
        long inactivos = empleadoRepository.countByEstado(EstadoEmpleado.INACTIVO);

        Optional<Planilla> ultimaPlanilla = planillaRepository.findAllByOrderByCreadoEnDesc().stream()
                .filter(p -> p.getEstado() == EstadoPlanilla.CALCULADA || p.getEstado() == EstadoPlanilla.APROBADA)
                .findFirst();

        DashboardResponse.DashboardResponseBuilder builder = DashboardResponse.builder()
                .totalEmpleadosActivos(activos)
                .totalEmpleadosInactivos(inactivos);

        ultimaPlanilla.ifPresent(p -> {
            BigDecimal deducciones = p.getTotalIsss().add(p.getTotalAfp()).add(p.getTotalIsr());
            builder.ultimoPeriodo(p.getPeriodoMes())
                    .totalBrutoUltimoPeriodo(p.getTotalBruto())
                    .totalNetoUltimoPeriodo(p.getTotalNeto())
                    .totalDeduccionesUltimoPeriodo(deducciones);
        });

        return builder.build();
    }
}
