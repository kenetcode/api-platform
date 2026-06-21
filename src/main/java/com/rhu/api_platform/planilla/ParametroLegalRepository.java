package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.entity.ParametroLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParametroLegalRepository extends JpaRepository<ParametroLegal, Long> {
    Optional<ParametroLegal> findByClave(String clave);
}
