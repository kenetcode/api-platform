package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.entity.Incapacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface IncapacidadRepository extends JpaRepository<Incapacidad, Long> {

    List<Incapacidad> findByEmpleadoId(Long empleadoId);

    @Query("SELECT i FROM Incapacidad i WHERE i.empleado.id = :empleadoId " +
           "AND NOT (i.fechaFin < :inicio OR i.fechaInicio > :fin)")
    List<Incapacidad> findSolapadas(@Param("empleadoId") Long empleadoId,
                                    @Param("inicio") LocalDate inicio,
                                    @Param("fin") LocalDate fin);

    @Query("SELECT SUM(i.dias) FROM Incapacidad i " +
           "WHERE i.empleado.id = :empleadoId " +
           "AND i.fechaInicio >= :inicioMes AND i.fechaFin <= :finMes")
    Integer sumDiasIncapacidadEnMes(@Param("empleadoId") Long empleadoId,
                                     @Param("inicioMes") LocalDate inicioMes,
                                     @Param("finMes") LocalDate finMes);
}
