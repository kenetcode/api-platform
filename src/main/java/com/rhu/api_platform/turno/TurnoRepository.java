package com.rhu.api_platform.turno;

import com.rhu.api_platform.turno.entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findAllByOrderByNombreAsc();

    List<Turno> findAllByEstadoOrderByNombreAsc(com.rhu.api_platform.turno.entity.EstadoTurno estado);
}
