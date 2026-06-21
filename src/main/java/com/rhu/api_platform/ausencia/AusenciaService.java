package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.dto.*;
import com.rhu.api_platform.ausencia.entity.*;
import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.EmpleadoRepository;
import com.rhu.api_platform.empleado.entity.Empleado;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AusenciaService {

    private final AusenciaRepository ausenciaRepository;
    private final IncapacidadRepository incapacidadRepository;
    private final EmpleadoRepository empleadoRepository;

    // ========== AUSENCIAS ==========

    @Transactional
    public AusenciaResponse crearAusencia(Long empleadoId, AusenciaRequest req) {
        Empleado empleado = obtenerEmpleadoActivo(empleadoId);
        validarFechas(req.getFechaInicio(), req.getFechaFin());

        List<Ausencia> solapadas = ausenciaRepository.findSolapadas(empleadoId, req.getFechaInicio(), req.getFechaFin());
        if (!solapadas.isEmpty()) {
            throw new ConflictoException("Las fechas se solapan con una ausencia ya registrada para este empleado.");
        }

        Ausencia ausencia = Ausencia.builder()
                .empleado(empleado)
                .tipo(req.getTipo())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .justificada(Boolean.TRUE.equals(req.getJustificada()))
                .observacion(req.getObservacion())
                .build();
        return toResponse(ausenciaRepository.save(ausencia));
    }

    public List<AusenciaResponse> listarAusencias(Long empleadoId) {
        obtenerEmpleadoActivo(empleadoId);
        return ausenciaRepository.findByEmpleadoId(empleadoId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AusenciaResponse actualizarAusencia(Long empleadoId, Long ausenciaId, AusenciaRequest req) {
        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Ausencia no encontrada: " + ausenciaId));
        validarFechas(req.getFechaInicio(), req.getFechaFin());

        List<Ausencia> solapadas = ausenciaRepository.findSolapadas(empleadoId, req.getFechaInicio(), req.getFechaFin())
                .stream().filter(a -> !a.getId().equals(ausenciaId)).toList();
        if (!solapadas.isEmpty()) {
            throw new ConflictoException("Las fechas se solapan con otra ausencia ya registrada.");
        }

        ausencia.setTipo(req.getTipo());
        ausencia.setFechaInicio(req.getFechaInicio());
        ausencia.setFechaFin(req.getFechaFin());
        ausencia.setJustificada(Boolean.TRUE.equals(req.getJustificada()));
        ausencia.setObservacion(req.getObservacion());
        return toResponse(ausenciaRepository.save(ausencia));
    }

    @Transactional
    public void eliminarAusencia(Long empleadoId, Long ausenciaId) {
        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .filter(a -> a.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Ausencia no encontrada: " + ausenciaId));
        ausenciaRepository.delete(ausencia);
    }

    public List<AusenciaResponse> listarPorPeriodo(String periodo) {
        YearMonth ym = YearMonth.parse(periodo);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();
        return ausenciaRepository.findByPeriodo(inicio, fin)
                .stream().map(this::toResponse).toList();
    }

    // ========== INCAPACIDADES ==========

    @Transactional
    public IncapacidadResponse crearIncapacidad(Long empleadoId, IncapacidadRequest req) {
        Empleado empleado = obtenerEmpleadoActivo(empleadoId);
        validarFechas(req.getFechaInicio(), req.getFechaFin());

        List<Incapacidad> solapadas = incapacidadRepository.findSolapadas(empleadoId, req.getFechaInicio(), req.getFechaFin());
        if (!solapadas.isEmpty()) {
            throw new ConflictoException("Las fechas se solapan con una incapacidad ya registrada para este empleado.");
        }

        Incapacidad incapacidad = Incapacidad.builder()
                .empleado(empleado)
                .tipo(req.getTipo())
                .fechaInicio(req.getFechaInicio())
                .fechaFin(req.getFechaFin())
                .dias(req.getDias())
                .documentoUrl(req.getDocumentoUrl())
                .build();
        return toResponse(incapacidadRepository.save(incapacidad));
    }

    public List<IncapacidadResponse> listarIncapacidades(Long empleadoId) {
        obtenerEmpleadoActivo(empleadoId);
        return incapacidadRepository.findByEmpleadoId(empleadoId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public IncapacidadResponse actualizarIncapacidad(Long empleadoId, Long incId, IncapacidadRequest req) {
        Incapacidad incapacidad = incapacidadRepository.findById(incId)
                .filter(i -> i.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Incapacidad no encontrada: " + incId));
        validarFechas(req.getFechaInicio(), req.getFechaFin());

        List<Incapacidad> solapadas = incapacidadRepository.findSolapadas(empleadoId, req.getFechaInicio(), req.getFechaFin())
                .stream().filter(i -> !i.getId().equals(incId)).toList();
        if (!solapadas.isEmpty()) {
            throw new ConflictoException("Las fechas se solapan con otra incapacidad ya registrada.");
        }

        incapacidad.setTipo(req.getTipo());
        incapacidad.setFechaInicio(req.getFechaInicio());
        incapacidad.setFechaFin(req.getFechaFin());
        incapacidad.setDias(req.getDias());
        incapacidad.setDocumentoUrl(req.getDocumentoUrl());
        return toResponse(incapacidadRepository.save(incapacidad));
    }

    @Transactional
    public void eliminarIncapacidad(Long empleadoId, Long incId) {
        Incapacidad incapacidad = incapacidadRepository.findById(incId)
                .filter(i -> i.getEmpleado().getId().equals(empleadoId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Incapacidad no encontrada: " + incId));
        incapacidadRepository.delete(incapacidad);
    }

    // ========== HELPERS ==========

    private Empleado obtenerEmpleadoActivo(Long empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + empleadoId));
        if (empleado.getEstado() != EstadoEmpleado.ACTIVO) {
            throw new ValidacionNegocioException("El empleado no está activo.");
        }
        return empleado;
    }

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (fin.isBefore(inicio)) {
            throw new ValidacionNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
    }

    private AusenciaResponse toResponse(Ausencia a) {
        return AusenciaResponse.builder()
                .id(a.getId())
                .empleadoId(a.getEmpleado().getId())
                .nombreEmpleado(a.getEmpleado().getNombre() + " " + a.getEmpleado().getApellido())
                .tipo(a.getTipo())
                .fechaInicio(a.getFechaInicio())
                .fechaFin(a.getFechaFin())
                .justificada(a.getJustificada())
                .observacion(a.getObservacion())
                .build();
    }

    private IncapacidadResponse toResponse(Incapacidad i) {
        return IncapacidadResponse.builder()
                .id(i.getId())
                .empleadoId(i.getEmpleado().getId())
                .nombreEmpleado(i.getEmpleado().getNombre() + " " + i.getEmpleado().getApellido())
                .tipo(i.getTipo())
                .fechaInicio(i.getFechaInicio())
                .fechaFin(i.getFechaFin())
                .dias(i.getDias())
                .documentoUrl(i.getDocumentoUrl())
                .build();
    }
}
