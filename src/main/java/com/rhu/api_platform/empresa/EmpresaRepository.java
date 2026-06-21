package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
