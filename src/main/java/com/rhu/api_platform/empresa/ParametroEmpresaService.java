package com.rhu.api_platform.empresa;

import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.empresa.dto.ParametroEmpresaRequest;
import com.rhu.api_platform.empresa.dto.ParametroEmpresaResponse;
import com.rhu.api_platform.empresa.entity.ParametroEmpresa;
import com.rhu.api_platform.empresa.entity.TipoParametroEmpresa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParametroEmpresaService {

    public static final String ASUME_3_DIAS_INCAPACIDAD = "EMPRESA_ASUME_3_DIAS_INCAPACIDAD";
    public static final String PORCENTAJE_PAGO_3_DIAS = "EMPRESA_PORCENTAJE_PAGO_3_DIAS";
    public static final String FECHA_PAGO_AGUINALDO = "EMPRESA_FECHA_PAGO_AGUINALDO";

    private static final boolean DEFAULT_ASUME_3_DIAS = true;
    private static final BigDecimal DEFAULT_PORCENTAJE_3_DIAS = new BigDecimal("100.00");

    private final ParametroEmpresaRepository parametroEmpresaRepository;

    @Transactional
    public ParametroEmpresaResponse crearOActualizar(ParametroEmpresaRequest req) {
        validarParametro(req);

        ParametroEmpresa param = parametroEmpresaRepository.findByClave(req.getClave())
                .orElse(ParametroEmpresa.builder().clave(req.getClave()).build());

        param.setValor(req.getValor());
        param.setTipo(req.getTipo());
        param.setDescripcion(req.getDescripcion());
        param.setVigencia(req.getVigencia());

        return toResponse(parametroEmpresaRepository.save(param));
    }

    public List<ParametroEmpresaResponse> listar() {
        return parametroEmpresaRepository.findAllByOrderByClaveAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    public ParametroEmpresaResponse obtenerPorClave(String clave) {
        return parametroEmpresaRepository.findByClave(clave)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Parámetro no encontrado: " + clave));
    }

    // ===== Métodos tipados con fallback =====

    public boolean asumePrimeros3DiasIncapacidad() {
        return parametroEmpresaRepository.findByClave(ASUME_3_DIAS_INCAPACIDAD)
                .map(p -> Boolean.parseBoolean(p.getValor()))
                .orElse(DEFAULT_ASUME_3_DIAS);
    }

    public BigDecimal porcentajePagoPrimeros3Dias() {
        return parametroEmpresaRepository.findByClave(PORCENTAJE_PAGO_3_DIAS)
                .map(p -> new BigDecimal(p.getValor()))
                .orElse(DEFAULT_PORCENTAJE_3_DIAS);
    }

    public LocalDate fechaPagoAguinaldo() {
        return parametroEmpresaRepository.findByClave(FECHA_PAGO_AGUINALDO)
                .map(p -> LocalDate.parse(p.getValor()))
                .orElseGet(() -> LocalDate.of(LocalDate.now().getYear(), Month.DECEMBER, 20));
    }

    // ===== Validaciones =====

    private void validarParametro(ParametroEmpresaRequest req) {
        if (req.getTipo() == TipoParametroEmpresa.BOOLEAN) {
            String v = req.getValor().trim().toLowerCase();
            if (!v.equals("true") && !v.equals("false")) {
                throw new ValidacionNegocioException("Valor booleano inválido. Use 'true' o 'false'.");
            }
        }

        if (req.getTipo() == TipoParametroEmpresa.DECIMAL) {
            try {
                new BigDecimal(req.getValor());
            } catch (NumberFormatException e) {
                throw new ValidacionNegocioException("Valor decimal inválido.");
            }
        }

        if (req.getTipo() == TipoParametroEmpresa.DATE) {
            try {
                LocalDate.parse(req.getValor());
            } catch (DateTimeParseException e) {
                throw new ValidacionNegocioException("Valor de fecha inválido. Use formato ISO (YYYY-MM-DD).");
            }
        }

        if (PORCENTAJE_PAGO_3_DIAS.equals(req.getClave())) {
            BigDecimal pct = new BigDecimal(req.getValor());
            if (pct.compareTo(BigDecimal.ZERO) < 0 || pct.compareTo(new BigDecimal("100")) > 0) {
                throw new ValidacionNegocioException("El porcentaje debe estar entre 0 y 100.");
            }
        }

        if (FECHA_PAGO_AGUINALDO.equals(req.getClave())) {
            LocalDate fecha = LocalDate.parse(req.getValor());
            LocalDate inicioVentana = LocalDate.of(fecha.getYear(), Month.OCTOBER, 20);
            LocalDate finVentana = LocalDate.of(fecha.getYear(), Month.DECEMBER, 20);
            if (fecha.isBefore(inicioVentana) || fecha.isAfter(finVentana)) {
                throw new ValidacionNegocioException(
                        "La fecha de pago del aguinaldo debe estar entre el 20 de octubre y el 20 de diciembre.");
            }
        }
    }

    private ParametroEmpresaResponse toResponse(ParametroEmpresa p) {
        return ParametroEmpresaResponse.builder()
                .id(p.getId())
                .clave(p.getClave())
                .valor(p.getValor())
                .tipo(p.getTipo())
                .descripcion(p.getDescripcion())
                .vigencia(p.getVigencia())
                .creadoEn(p.getCreadoEn())
                .actualizadoEn(p.getActualizadoEn())
                .build();
    }
}
