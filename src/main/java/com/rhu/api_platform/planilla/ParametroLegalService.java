package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.entity.ParametroLegal;
import com.rhu.api_platform.planilla.motor.ParametrosCalculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParametroLegalService {

    private final ParametroLegalRepository parametroLegalRepository;

    public List<ParametroLegal> listar() {
        return parametroLegalRepository.findAll();
    }

    /**
     * Carga los parámetros desde la BD. Si no existen, usa los valores
     * legales vigentes para El Salvador 2026 (hardcoded como fallback).
     */
    public ParametrosCalculo cargarParametros() {
        Map<String, String> mapa = parametroLegalRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ParametroLegal::getClave, ParametroLegal::getValor));

        return ParametrosCalculo.builder()
                .isssPorc(bd(mapa, "ISSS_TRABAJADOR_PORC", "0.03"))
                .isssBasMaxima(bd(mapa, "ISSS_BASE_MAXIMA", "1000.00"))
                .aportePatronalIsssPorc(bd(mapa, "ISSS_PATRONO_PORC", "0.075"))
                .afpPorc(bd(mapa, "AFP_TRABAJADOR_PORC", "0.0725"))
                .aportePatronalAfpPorc(bd(mapa, "AFP_PATRONO_PORC", "0.0875"))
                .isrExento(bd(mapa, "ISR_TRAMO1_TOPE", "550.00"))
                .isrTramo2Inicio(bd(mapa, "ISR_TRAMO2_INICIO", "550.01"))
                .isrTramo2Fin(bd(mapa, "ISR_TRAMO2_FIN", "895.24"))
                .isrTramo2Porc(bd(mapa, "ISR_TRAMO2_PORC", "0.10"))
                .isrTramo2Cuota(bd(mapa, "ISR_TRAMO2_CUOTA", "17.67"))
                .isrTramo3Inicio(bd(mapa, "ISR_TRAMO3_INICIO", "895.25"))
                .isrTramo3Fin(bd(mapa, "ISR_TRAMO3_FIN", "2038.10"))
                .isrTramo3Porc(bd(mapa, "ISR_TRAMO3_PORC", "0.20"))
                .isrTramo3Cuota(bd(mapa, "ISR_TRAMO3_CUOTA", "60.00"))
                .isrTramo4Inicio(bd(mapa, "ISR_TRAMO4_INICIO", "2038.11"))
                .isrTramo4Porc(bd(mapa, "ISR_TRAMO4_PORC", "0.30"))
                .isrTramo4Cuota(bd(mapa, "ISR_TRAMO4_CUOTA", "288.57"))
                .horasMensuales(bd(mapa, "HORAS_MENSUALES", "240"))
                .build();
    }

    private BigDecimal bd(Map<String, String> mapa, String clave, String defecto) {
        return new BigDecimal(mapa.getOrDefault(clave, defecto));
    }
}
