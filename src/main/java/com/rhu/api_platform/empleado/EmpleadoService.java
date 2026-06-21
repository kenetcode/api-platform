package com.rhu.api_platform.empleado;

import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empleado.dto.*;
import com.rhu.api_platform.empleado.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    private static final Map<SectorEmpleado, BigDecimal> SALARIOS_MINIMOS = Map.of(
        SectorEmpleado.COMERCIO_SERVICIOS, new BigDecimal("408.80"),
        SectorEmpleado.INDUSTRIA, new BigDecimal("408.80"),
        SectorEmpleado.MAQUILA_TEXTIL, new BigDecimal("402.26"),
        SectorEmpleado.AGROPECUARIO, new BigDecimal("272.72")
    );

    @Transactional
    public EmpleadoResponse crear(CrearEmpleadoRequest req) {
        if (empleadoRepository.existsByDui(req.getDui())) {
            throw new ConflictoException("Ya existe un empleado con DUI: " + req.getDui());
        }
        validarSalarioMinimo(req.getSalarioBase(), req.getSector());

        Empleado empleado = Empleado.builder()
                .nombre(req.getNombre())
                .apellido(req.getApellido())
                .dui(req.getDui())
                .nit(req.getNit())
                .correo(req.getCorreo())
                .telefono(req.getTelefono())
                .fechaNacimiento(req.getFechaNacimiento())
                .genero(req.getGenero())
                .direccion(req.getDireccion())
                .municipio(req.getMunicipio())
                .departamento(req.getDepartamento())
                .cargo(req.getCargo())
                .departamentoLab(req.getDepartamentoLab())
                .fechaIngreso(req.getFechaIngreso())
                .tipoContrato(req.getTipoContrato())
                .salarioBase(req.getSalarioBase())
                .sector(req.getSector())
                .afp(req.getAfp())
                .numIsss(req.getNumIsss())
                .contactoEmergenciaNombre(req.getContactoEmergenciaNombre())
                .contactoEmergenciaTelefono(req.getContactoEmergenciaTelefono())
                .estado(EstadoEmpleado.ACTIVO)
                .esBorrador(Boolean.TRUE.equals(req.getEsBorrador()))
                .build();

        return toResponse(empleadoRepository.save(empleado));
    }

    public List<EmpleadoResponse> listar(String q, String estadoStr, String departamento) {
        EstadoEmpleado estado = null;
        if (estadoStr != null && !estadoStr.isBlank()) {
            try { estado = EstadoEmpleado.valueOf(estadoStr.toUpperCase()); }
            catch (IllegalArgumentException e) { /* ignorar filtro inválido */ }
        }
        return empleadoRepository.buscar(q, estado, departamento)
                .stream().map(this::toResponse).toList();
    }

    public EmpleadoResponse obtener(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional
    public EmpleadoResponse actualizar(Long id, CrearEmpleadoRequest req) {
        Empleado empleado = buscarPorId(id);
        if (!empleado.getDui().equals(req.getDui()) && empleadoRepository.existsByDui(req.getDui())) {
            throw new ConflictoException("Ya existe otro empleado con DUI: " + req.getDui());
        }
        validarSalarioMinimo(req.getSalarioBase(), req.getSector());

        empleado.setNombre(req.getNombre());
        empleado.setApellido(req.getApellido());
        empleado.setDui(req.getDui());
        empleado.setNit(req.getNit());
        empleado.setCorreo(req.getCorreo());
        empleado.setTelefono(req.getTelefono());
        empleado.setFechaNacimiento(req.getFechaNacimiento());
        empleado.setGenero(req.getGenero());
        empleado.setDireccion(req.getDireccion());
        empleado.setMunicipio(req.getMunicipio());
        empleado.setDepartamento(req.getDepartamento());
        empleado.setCargo(req.getCargo());
        empleado.setDepartamentoLab(req.getDepartamentoLab());
        empleado.setFechaIngreso(req.getFechaIngreso());
        empleado.setTipoContrato(req.getTipoContrato());
        empleado.setSalarioBase(req.getSalarioBase());
        empleado.setSector(req.getSector());
        empleado.setAfp(req.getAfp());
        empleado.setNumIsss(req.getNumIsss());
        empleado.setContactoEmergenciaNombre(req.getContactoEmergenciaNombre());
        empleado.setContactoEmergenciaTelefono(req.getContactoEmergenciaTelefono());
        if (req.getEsBorrador() != null) empleado.setEsBorrador(req.getEsBorrador());

        return toResponse(empleadoRepository.save(empleado));
    }

    @Transactional
    public EmpleadoResponse cambiarEstado(Long id, EstadoEmpleado estado) {
        Empleado empleado = buscarPorId(id);
        empleado.setEstado(estado);
        return toResponse(empleadoRepository.save(empleado));
    }

    public ContadoresResponse obtenerContadores() {
        long activos = empleadoRepository.countByEstado(EstadoEmpleado.ACTIVO);
        long inactivos = empleadoRepository.countByEstado(EstadoEmpleado.INACTIVO);
        return ContadoresResponse.builder()
                .total(activos + inactivos)
                .activos(activos)
                .inactivos(inactivos)
                .build();
    }

    private Empleado buscarPorId(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + id));
    }

    private void validarSalarioMinimo(BigDecimal salario, SectorEmpleado sector) {
        if (sector == null) return;
        BigDecimal minimo = SALARIOS_MINIMOS.getOrDefault(sector, BigDecimal.ZERO);
        if (salario.compareTo(minimo) < 0) {
            throw new ValidacionNegocioException(
                "El salario $" + salario + " es menor al mínimo del sector " + sector + " ($" + minimo + ")");
        }
    }

    private EmpleadoResponse toResponse(Empleado e) {
        return EmpleadoResponse.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .apellido(e.getApellido())
                .dui(e.getDui())
                .nit(e.getNit())
                .correo(e.getCorreo())
                .telefono(e.getTelefono())
                .fechaNacimiento(e.getFechaNacimiento())
                .genero(e.getGenero())
                .direccion(e.getDireccion())
                .municipio(e.getMunicipio())
                .departamento(e.getDepartamento())
                .cargo(e.getCargo())
                .departamentoLab(e.getDepartamentoLab())
                .fechaIngreso(e.getFechaIngreso())
                .tipoContrato(e.getTipoContrato())
                .salarioBase(e.getSalarioBase())
                .sector(e.getSector())
                .afp(e.getAfp())
                .numIsss(e.getNumIsss())
                .contactoEmergenciaNombre(e.getContactoEmergenciaNombre())
                .contactoEmergenciaTelefono(e.getContactoEmergenciaTelefono())
                .estado(e.getEstado())
                .esBorrador(e.getEsBorrador())
                .creadoEn(e.getCreadoEn())
                .build();
    }
}
