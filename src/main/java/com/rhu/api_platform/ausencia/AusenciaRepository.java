package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.entity.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AusenciaRepository extends JpaRepository<Ausencia, Long> {

    List<Ausencia> findByEmpleadoId(Long empleadoId);

    @Query("SELECT a FROM Ausencia a WHERE a.empleado.id = :empleadoId " +
           "AND NOT (a.fechaFin < :inicio OR a.fechaInicio > :fin)")
    List<Ausencia> findSolapadas(@Param("empleadoId") Long empleadoId,
                                 @Param("inicio") LocalDate inicio,
                                 @Param("fin") LocalDate fin);

    @Query("SELECT a FROM Ausencia a WHERE a.fechaInicio >= :inicio AND a.fechaInicio <= :fin")
    List<Ausencia> findByPeriodo(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    @Query("SELECT SUM(DATEDIFF(a.fechaFin, a.fechaInicio) + 1) FROM Ausencia a " +
           "WHERE a.empleado.id = :empleadoId " +
           "AND a.fechaInicio >= :inicioMes AND a.fechaFin <= :finMes")
    Integer sumDiasAusenciaEnMes(@Param("empleadoId") Long empleadoId,
                                  @Param("inicioMes") LocalDate inicioMes,
                                  @Param("finMes") LocalDate finMes);
}
