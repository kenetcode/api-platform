package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.dto.AusenciaRequest;
import com.rhu.api_platform.ausencia.dto.AusenciaResponse;
import com.rhu.api_platform.ausencia.entity.Ausencia;
import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.Empleado;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AusenciaService {

    private final AusenciaRepository ausenciaRepository;
    private final EmpleadoRepository empleadoRepository;

    @Transactional
    public AusenciaResponse crear(Long empleadoId, AusenciaRequest req) {
        Empleado empleado = obtenerEmpleadoActivo(empleadoId);
        validarFechas(req.getFechaInicio(), req.getFechaFin());
        validarTipo(req);
        validarSolapamiento(empleadoId, req.getFechaInicio(), req.getFechaFin(), null);

        Ausencia ausencia = Ausencia.builder()
                .empleado(empleado)
                .tipo(req.getTipo())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .dias(calcularDias(req))
                .horas(req.getHoras())
                .documentoRespaldoUrl(req.getDocumentoRespaldoUrl())
                .pagoPorcentaje(req.getPagoPorcentaje())
                .observacion(req.getObservacion())
                .build();

        return toResponse(ausenciaRepository.save(ausencia));
    }

    public List<AusenciaResponse> listarPorEmpleado(Long empleadoId) {
        obtenerEmpleado(empleadoId);
        return ausenciaRepository.findByEmpleadoId(empleadoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AusenciaResponse> listarIncapacidadesPorEmpleado(Long empleadoId) {
        obtenerEmpleado(empleadoId);
        return ausenciaRepository.findByEmpleadoIdAndTipoIn(empleadoId,
                        List.of(TipoAusencia.INCAPACIDAD_COMUN, TipoAusencia.INCAPACIDAD_ISSS_TOTAL))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AusenciaResponse> listarPorPeriodo(String periodo) {
        YearMonth ym = YearMonth.parse(periodo);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        return ausenciaRepository.findByPeriodo(inicio, fin).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AusenciaResponse actualizar(Long empleadoId, Long ausenciaId, AusenciaRequest req) {
        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado: " + ausenciaId));

        validarFechas(req.getFechaInicio(), req.getFechaFin());
        validarTipo(req);
        validarSolapamiento(empleadoId, req.getFechaInicio(), req.getFechaFin(), ausenciaId);

        ausencia.setTipo(req.getTipo());
        ausencia.setFechaInicio(req.getFechaInicio());
        ausencia.setFechaFin(req.getFechaFin());
        ausencia.setDias(calcularDias(req));
        ausencia.setHoras(req.getHoras());
        ausencia.setDocumentoRespaldoUrl(req.getDocumentoRespaldoUrl());
        ausencia.setPagoPorcentaje(req.getPagoPorcentaje());
        ausencia.setObservacion(req.getObservacion());

        return toResponse(ausenciaRepository.save(ausencia));
    }

    @Transactional
    public void eliminar(Long empleadoId, Long ausenciaId) {
        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro no encontrado: " + ausenciaId));
        ausenciaRepository.delete(ausencia);
    }

    private Empleado obtenerEmpleadoActivo(Long empleadoId) {
        Empleado empleado = obtenerEmpleado(empleadoId);
        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new ValidacionNegocioException("El empleado no está activo.");
        }
        return empleado;
    }

    private Empleado obtenerEmpleado(Long empleadoId) {
        return empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + empleadoId));
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            throw new ValidacionNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    private void validarTipo(AusenciaRequest req) {
        if (req.getTipo() == TipoAusencia.AUSENCIA_POR_HORAS) {
            if (req.getHoras() == null || req.getHoras().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidacionNegocioException("Para 'Ausencia por Horas' debe indicar las horas.");
            }
            if (req.getDias() != null && req.getDias() > 1) {
                throw new ValidacionNegocioException("'Ausencia por Horas' no debe tener más de 1 día.");
            }
        } else {
            if (req.getHoras() != null) {
                throw new ValidacionNegocioException("Las horas solo aplican para 'Ausencia por Horas'.");
            }
        }

        if (req.getTipo() == TipoAusencia.INCAPACIDAD_COMUN && req.getPagoPorcentaje() != null) {
            if (req.getPagoPorcentaje().compareTo(BigDecimal.ZERO) < 0
                    || req.getPagoPorcentaje().compareTo(new BigDecimal("100")) > 0) {
                throw new ValidacionNegocioException("El porcentaje de pago debe estar entre 0 y 100.");
            }
        }

        if (req.getTipo() != TipoAusencia.INCAPACIDAD_COMUN && req.getPagoPorcentaje() != null) {
            throw new ValidacionNegocioException("El porcentaje de pago solo aplica para 'Incapacidad Común'.");
        }
    }

    private void validarSolapamiento(Long empleadoId, LocalDate inicio, LocalDate fin, Long excluirId) {
        List<Ausencia> solapadas = ausenciaRepository.findSolapadas(empleadoId, inicio, fin);
        if (excluirId != null) {
            solapadas = solapadas.stream().filter(a -> !a.getId().equals(excluirId)).toList();
        }
        if (!solapadas.isEmpty()) {
            throw new ConflictoException("Las fechas se solapan con otro registro de ausencia/incapacidad del empleado.");
        }
    }

    private Integer calcularDias(AusenciaRequest req) {
        if (req.getDias() != null) {
            return req.getDias();
        }
        return (int) ChronoUnit.DAYS.between(req.getFechaInicio(), req.getFechaFin()) + 1;
    }

    private String descripcionTipo(TipoAusencia tipo) {
        return switch (tipo) {
            case INCAPACIDAD_COMUN -> "Incapacidad Común (enfermedad o accidente común)";
            case INCAPACIDAD_ISSS_TOTAL -> "Incapacidad ISSS Total (maternidad o riesgo profesional)";
            case PERMISO_CON_GOCE -> "Permiso con Goce de Sueldo";
            case PERMISO_SIN_GOCE -> "Permiso sin Goce de Sueldo";
            case FALTA_INJUSTIFICADA -> "Falta Injustificada";
            case AUSENCIA_POR_HORAS -> "Ausencia por Horas";
        };
    }

    private AusenciaResponse toResponse(Ausencia a) {
        return AusenciaResponse.builder()
                .id(a.getId())
                .empleadoId(a.getEmpleado().getId())
                .nombreEmpleado(a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido())
                .tipo(a.getTipo())
                .tipoDescripcion(descripcionTipo(a.getTipo()))
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .dias(a.getDias())
                .horas(a.getHoras())
                .documentoRespaldoUrl(a.getDocumentoRespaldoUrl())
                .pagoPorcentaje(a.getPagoPorcentaje())
                .conGoceSueldo(a.conGoceSueldo())
                .afectaSeptimoDia(a.afectaSeptimoDia())
                .observacion(a.getObservacion())
                .build();
    }
}
