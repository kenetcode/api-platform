package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.entity.Planilla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanillaRepository extends JpaRepository<Planilla, Long> {
    List<Planilla> findAllByOrderByCreadoEnDesc();
}
