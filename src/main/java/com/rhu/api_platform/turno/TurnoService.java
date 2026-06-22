package com.rhu.api_platform.turno;

import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.turno.dto.TurnoRequest;
import com.rhu.api_platform.turno.dto.TurnoResponse;
import com.rhu.api_platform.turno.entity.EstadoTurno;
import com.rhu.api_platform.turno.entity.Turno;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final EmpleadoRepository empleadoRepository;

    @Transactional
    public TurnoResponse crear(TurnoRequest req) {
        validarHorario(req);

        Turno turno = Turno.builder()
                .nombre(req.getNombre().trim())
                .descripcion(req.getDescripcion())
                .diasLaborables(req.getDiasLaborables())
                .horaEntrada(req.getHoraEntrada())
                .horaSalida(req.getHoraSalida())
                .horasOrdinariasDiarias(req.getHorasOrdinariasDiarias())
                .estado(EstadoTurno.ACTIVO)
                .build();

        return toResponse(turnoRepository.save(turno));
    }

    public List<TurnoResponse> listar() {
        return turnoRepository.findAllByOrderByNombreAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public TurnoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public TurnoResponse actualizar(Long id, TurnoRequest req) {
        Turno turno = buscarPorId(id);
        validarHorario(req);

        turno.setNombre(req.getNombre().trim());
        turno.setDescripcion(req.getDescripcion());
        turno.setDiasLaborables(req.getDiasLaborables());
        turno.setHoraEntrada(req.getHoraEntrada());
        turno.setHoraSalida(req.getHoraSalida());
        turno.setHorasOrdinariasDiarias(req.getHorasOrdinariasDiarias());

        return toResponse(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponse cambiarEstado(Long id, EstadoTurno estado) {
        Turno turno = buscarPorId(id);
        if (estado == EstadoTurno.INACTIVO && turnoTieneEmpleadosActivos(id)) {
            throw new ValidacionNegocioException(
                    "No se puede inactivar el turno porque tiene empleados activos asignados.");
        }
        turno.setEstado(estado);
        return toResponse(turnoRepository.save(turno));
    }

    private Turno buscarPorId(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno no encontrado: " + id));
    }

    private boolean turnoTieneEmpleadosActivos(Long turnoId) {
        return empleadoRepository.countByTurnoIdAndEstadoActivo(turnoId) > 0;
    }

    private void validarHorario(TurnoRequest req) {
        if (!req.getHoraSalida().isAfter(req.getHoraEntrada())) {
            throw new ValidacionNegocioException("La hora de salida debe ser posterior a la hora de entrada.");
        }

        long minutos = Duration.between(req.getHoraEntrada(), req.getHoraSalida()).toMinutes();
        BigDecimal horasCalculadas = BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        // Se permite una diferencia de hasta 0.5 horas entre el horario y las horas ordinarias declaradas
        BigDecimal diferencia = horasCalculadas.subtract(req.getHorasOrdinariasDiarias()).abs();
        if (diferencia.compareTo(new BigDecimal("0.5")) > 0) {
            throw new ValidacionNegocioException(
                    "Las horas ordinarias diarias no coinciden con el rango del horario. " +
                    "Esperado aproximado: " + horasCalculadas + " horas.");
        }
    }

    private TurnoResponse toResponse(Turno t) {
        return TurnoResponse.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .diasLaborables(t.getDiasLaborables())
                .horaEntrada(t.getHoraEntrada())
                .horaSalida(t.getHoraSalida())
                .horasOrdinariasDiarias(t.getHorasOrdinariasDiarias())
                .estado(t.getEstado())
                .creadoEn(t.getCreadoEn())
                .actualizadoEn(t.getActualizadoEn())
                .build();
    }
}
