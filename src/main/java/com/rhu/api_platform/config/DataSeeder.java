package com.rhu.api_platform.config;

import com.rhu.api_platform.ausencia.AusenciaRepository;
import com.rhu.api_platform.ausencia.entity.Ausencia;
import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import com.rhu.api_platform.empresa.EmpresaRepository;
import com.rhu.api_platform.empresa.entity.Empresa;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.*;
import com.rhu.api_platform.planilla.DetallePlanillaRepository;
import com.rhu.api_platform.planilla.ParametroLegalRepository;
import com.rhu.api_platform.planilla.PlanillaRepository;
import com.rhu.api_platform.planilla.entity.DetallePlanilla;
import com.rhu.api_platform.planilla.entity.EstadoPlanilla;
import com.rhu.api_platform.planilla.entity.ParametroLegal;
import com.rhu.api_platform.planilla.entity.Planilla;
import com.rhu.api_platform.planilla.entity.TipoPlanilla;
import com.rhu.api_platform.turno.TurnoRepository;
import com.rhu.api_platform.turno.entity.DiaSemana;
import com.rhu.api_platform.turno.entity.Turno;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * Rellena la base de datos con datos de prueba al iniciar la aplicación
 * si no existe ningún empleado. Útil para probar la APP en Docker o local.
 *
 * Se ejecuta después de InicializadorDatos (@Order(2)).
 */
@Component
@RequiredArgsConstructor
@Order(2)
@ConditionalOnProperty(name = "app.seeder.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final EmpresaRepository empresaRepository;
    private final ParametroLegalRepository parametroLegalRepository;
    private final TurnoRepository turnoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AusenciaRepository ausenciaRepository;
    private final PlanillaRepository planillaRepository;
    private final DetallePlanillaRepository detallePlanillaRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (empleadoRepository.count() > 0) {
            log.info("DataSeeder: ya existen empleados en la base de datos. Se omite la carga de datos de prueba.");
            return;
        }

        log.info("DataSeeder: cargando datos de prueba...");

        crearEmpresa();
        crearParametrosLegales();
        Turno turnoDiurno = crearTurnos();
        List<Empleado> empleados = crearEmpleados(turnoDiurno);
        crearAusencias(empleados);
        crearPlanillaDePrueba(empleados);

        log.info("DataSeeder: datos de prueba cargados correctamente.");
        log.info("  - Empleados: {}", empleados.size());
        log.info("  - Usuario admin: admin / Admin1234!");
        log.info("  - Planilla de prueba creada en estado BORRADOR (calcular con POST /api/planillas/{id}/calcular).");
    }

    private void crearEmpresa() {
        if (empresaRepository.count() > 0) {
            return;
        }
        empresaRepository.save(Empresa.builder()
                .nombre("Supermercado La Cesta, S.A. de C.V.")
                .logoUrl("/assets/logo.png")
                .direccion("San Salvador, El Salvador")
                .nit("0614-000000-000-0")
                .build());
        log.info("DataSeeder: empresa de prueba creada.");
    }

    private void crearParametrosLegales() {
        if (parametroLegalRepository.count() > 0) {
            return;
        }

        List<ParametroLegal> parametros = List.of(
                parametro("ISSS_TRABAJADOR_PORC", "0.03", "ISSS trabajador 3%"),
                parametro("ISSS_BASE_MAXIMA", "1000.00", "Base máxima ISSS"),
                parametro("ISSS_PATRONO_PORC", "0.075", "ISSS patronal 7.5%"),
                parametro("AFP_TRABAJADOR_PORC", "0.0725", "AFP trabajador 7.25%"),
                parametro("AFP_PATRONO_PORC", "0.0875", "AFP patronal 8.75%"),
                parametro("ISR_TRAMO1_TOPE", "275.00", "ISR exento (quincenal)"),
                parametro("ISR_TRAMO2_INICIO", "275.01", "ISR Tramo II inicio"),
                parametro("ISR_TRAMO2_FIN", "447.62", "ISR Tramo II fin"),
                parametro("ISR_TRAMO2_PORC", "0.10", "ISR Tramo II %"),
                parametro("ISR_TRAMO2_CUOTA", "8.83", "ISR Tramo II cuota"),
                parametro("ISR_TRAMO3_INICIO", "447.63", "ISR Tramo III inicio"),
                parametro("ISR_TRAMO3_FIN", "1019.05", "ISR Tramo III fin"),
                parametro("ISR_TRAMO3_PORC", "0.20", "ISR Tramo III %"),
                parametro("ISR_TRAMO3_CUOTA", "30.00", "ISR Tramo III cuota"),
                parametro("ISR_TRAMO4_INICIO", "1019.06", "ISR Tramo IV inicio"),
                parametro("ISR_TRAMO4_PORC", "0.30", "ISR Tramo IV %"),
                parametro("ISR_TRAMO4_CUOTA", "144.28", "ISR Tramo IV cuota"),
                parametro("HORAS_MENSUALES", "240", "Horas laborales mensuales"),
                parametro("QUINCENA_25_ACTIVA", "true", "Quincena 25 activa"),
                parametro("QUINCENA_25_TOPE_SALARIO", "1500.00", "Tope salarial Quincena 25")
        );

        parametroLegalRepository.saveAll(parametros);
        log.info("DataSeeder: parámetros legales de prueba creados.");
    }

    private ParametroLegal parametro(String clave, String valor, String descripcion) {
        return ParametroLegal.builder()
                .clave(clave)
                .valor(valor)
                .descripcion(descripcion)
                .vigencia(LocalDate.of(2026, 1, 1))
                .build();
    }

    private Turno crearTurnos() {
        if (turnoRepository.count() > 0) {
            return turnoRepository.findAll().get(0);
        }

        Turno turno = Turno.builder()
                .nombre("Turno Tienda Lunes-Sábado")
                .descripcion("Horario regular de tienda")
                .diasLaborables(Set.of(
                        DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                        DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO))
                .horaEntrada(LocalTime.of(8, 0))
                .horaSalida(LocalTime.of(17, 0))
                .horasOrdinariasDiarias(new BigDecimal("8"))
                .build();
        turnoRepository.save(turno);
        log.info("DataSeeder: turno de prueba creado.");
        return turno;
    }

    private List<Empleado> crearEmpleados(Turno turno) {
        List<Empleado> empleados = List.of(
                empleado("Carlos", "Martínez López", "12345678-9", "0614-123456-001-0",
                        "carlos.martinez@lacesta.com", "72345678", LocalDate.of(1990, 3, 15),
                        Genero.MASCULINO, "Cajero", "Ventas",
                        LocalDate.of(2020, 5, 10), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("450.00"), turno),
                empleado("Ana", "García Fernández", "23456789-0", "0614-234567-002-0",
                        "ana.garcia@lacesta.com", "73456789", LocalDate.of(1988, 7, 22),
                        Genero.FEMENINO, "Supervisora de Caja", "Ventas",
                        LocalDate.of(2019, 2, 1), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("650.00"), turno),
                empleado("Luis", "Hernández Pérez", "34567890-1", "0614-345678-003-0",
                        "luis.hernandez@lacesta.com", "74567890", LocalDate.of(1995, 11, 5),
                        Genero.MASCULINO, "Bodeguero", "Bodega",
                        LocalDate.of(2021, 8, 15), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("500.00"), turno),
                empleado("María", "López Castillo", "45678901-2", "0614-456789-004-0",
                        "maria.lopez@lacesta.com", "75678901", LocalDate.of(1992, 1, 30),
                        Genero.FEMENINO, "Limpiadora", "Limpieza",
                        LocalDate.of(2023, 3, 20), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("408.80"), turno),
                empleado("Pedro", "Ramírez Díaz", "56789012-3", "0614-567890-005-0",
                        "pedro.ramirez@lacesta.com", "76789012", LocalDate.of(1985, 9, 12),
                        Genero.MASCULINO, "Asistente RRHH", "Administración",
                        LocalDate.of(2018, 6, 1), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("800.00"), turno),
                empleado("Sofía", "Torres Vega", "67890123-4", "0614-678901-006-0",
                        "sofia.torres@lacesta.com", "77890123", LocalDate.of(1998, 4, 18),
                        Genero.FEMENINO, "Cajera", "Ventas",
                        LocalDate.of(2025, 7, 1), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("430.00"), turno),
                empleado("Jorge", "Molina Ruiz", "78901234-5", "0614-789012-007-0",
                        "jorge.molina@lacesta.com", "78901234", LocalDate.of(1980, 12, 3),
                        Genero.MASCULINO, "Gerente de Tienda", "Administración",
                        LocalDate.of(2015, 1, 10), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("1200.00"), turno),
                empleado("Elena", "Vásquez Cruz", "89012345-6", "0614-890123-008-0",
                        "elena.vasquez@lacesta.com", "79012345", LocalDate.of(1993, 6, 25),
                        Genero.FEMENINO, "Reponedora", "Bodega",
                        LocalDate.of(2022, 11, 15), TipoContrato.TIEMPO_COMPLETO,
                        new BigDecimal("475.00"), turno)
        );

        empleadoRepository.saveAll(empleados);
        log.info("DataSeeder: {} empleados de prueba creados.", empleados.size());
        return empleados;
    }

    private Empleado empleado(String nombre, String apellido, String dui, String nit, String correo,
                               String telefono, LocalDate fechaNacimiento, Genero genero,
                               String cargo, String departamentoLab, LocalDate fechaIngreso,
                               TipoContrato tipoContrato, BigDecimal salarioBase, Turno turno) {
        return Empleado.builder()
                .nombre(nombre)
                .apellido(apellido)
                .dui(dui)
                .nit(nit)
                .correo(correo)
                .telefono(telefono)
                .fechaNacimiento(fechaNacimiento)
                .genero(genero)
                .direccion("San Salvador, El Salvador")
                .municipio("San Salvador")
                .departamento("San Salvador")
                .cargo(cargo)
                .departamentoLab(departamentoLab)
                .fechaIngreso(fechaIngreso)
                .tipoContrato(tipoContrato)
                .salarioBase(salarioBase)
                .sector(SectorEmpleado.COMERCIO_SERVICIOS)
                .afp("CRECER")
                .numIsss("00" + dui.replace("-", "").substring(0, 6))
                .turno(turno)
                .estado(EstadoEmpleado.ACTIVO)
                .build();
    }

    private void crearAusencias(List<Empleado> empleados) {
        Empleado carlos = empleados.get(0);
        Empleado sofia = empleados.get(5);

        List<Ausencia> ausencias = List.of(
                Ausencia.builder()
                        .empleado(carlos)
                        .tipo(TipoAusencia.INCAPACIDAD_COMUN)
                        .fechaInicio(LocalDate.of(2026, 2, 10))
                        .fechaFin(LocalDate.of(2026, 2, 12))
                        .dias(3)
                        .observacion("Resfrío común")
                        .build(),
                Ausencia.builder()
                        .empleado(sofia)
                        .tipo(TipoAusencia.FALTA_INJUSTIFICADA)
                        .fechaInicio(LocalDate.of(2026, 2, 6))
                        .fechaFin(LocalDate.of(2026, 2, 6))
                        .dias(1)
                        .observacion("No se presentó a laborar")
                        .build()
        );

        ausenciaRepository.saveAll(ausencias);
        log.info("DataSeeder: {} ausencias de prueba creadas (1 incapacidad + 1 falta).", ausencias.size());
    }

    private void crearPlanillaDePrueba(List<Empleado> empleados) {
        if (planillaRepository.count() > 0) {
            return;
        }

        Planilla planilla = Planilla.builder()
                .periodoMes("2026-02")
                .numeroQuincena(1)
                .fechaInicio(LocalDate.of(2026, 2, 1))
                .fechaFin(LocalDate.of(2026, 2, 15))
                .tipo(TipoPlanilla.QUINCENAL)
                .estado(EstadoPlanilla.BORRADOR)
                .build();
        planillaRepository.save(planilla);

        // Crear detalles para cada empleado. Algunos tienen horas extras nocturnas de prueba.
        for (int i = 0; i < empleados.size(); i++) {
            Empleado empleado = empleados.get(i);
            BigDecimal horasExtraNocturnas = switch (i) {
                case 1 -> new BigDecimal("6.00");  // Ana
                case 4 -> new BigDecimal("4.00");  // Pedro
                case 6 -> new BigDecimal("8.00");  // Jorge
                default -> BigDecimal.ZERO;
            };

            DetallePlanilla detalle = DetallePlanilla.builder()
                    .planilla(planilla)
                    .empleado(empleado)
                    .diasLaborados(15)
                    .horasExtraDiurnas(BigDecimal.ZERO)
                    .horasExtraNocturnas(horasExtraNocturnas)
                    .comisiones(BigDecimal.ZERO)
                    .bonificaciones(BigDecimal.ZERO)
                    .descuentosVoluntarios(BigDecimal.ZERO)
                    .diasDescansoTrabajados(0)
                    .build();
            detallePlanillaRepository.save(detalle);
        }

        log.info("DataSeeder: planilla de prueba creada (id={}) en estado BORRADOR con {} detalles.",
                planilla.getId(), empleados.size());
    }
}
