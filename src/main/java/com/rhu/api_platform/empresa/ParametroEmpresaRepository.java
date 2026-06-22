package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.entity.ParametroEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParametroEmpresaRepository extends JpaRepository<ParametroEmpresa, Long> {

    Optional<ParametroEmpresa> findByClave(String clave);

    List<ParametroEmpresa> findAllByOrderByClaveAsc();
}
