package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.entity.DetallePlanilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DetallePlanillaRepository extends JpaRepository<DetallePlanilla, Long> {
    List<DetallePlanilla> findByPlanillaId(Long planillaId);
    Optional<DetallePlanilla> findByPlanillaIdAndEmpleadoId(Long planillaId, Long empleadoId);
}
