package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.dto.DashboardResponse;
import com.rhu.api_platform.empresa.dto.EmpresaResponse;
import com.rhu.api_platform.empresa.entity.Empresa;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final EmpleadoRepository empleadoRepository;

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

        return DashboardResponse.builder()
                .totalEmpleadosActivos(activos)
                .totalEmpleadosInactivos(inactivos)
                .build();
    }
}
