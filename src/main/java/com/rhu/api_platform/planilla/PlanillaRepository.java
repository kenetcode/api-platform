package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.entity.Planilla;
import com.rhu.api_platform.planilla.entity.TipoPlanilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanillaRepository extends JpaRepository<Planilla, Long> {
    List<Planilla> findAllByOrderByCreadoEnDesc();
    boolean existsByTipoAndPeriodoMes(TipoPlanilla tipo, String periodoMes);
}
