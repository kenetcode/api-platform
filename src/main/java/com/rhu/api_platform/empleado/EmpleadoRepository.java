package com.rhu.api_platform.empleado;

import com.rhu.api_platform.empleado.entity.Empleado;
import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    boolean existsByDui(String dui);

    long countByEstado(EstadoEmpleado estado);

    @Query("SELECT e FROM Empleado e WHERE " +
           "(:q IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(e.apellido) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(e.departamentoLab) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:estado IS NULL OR e.estado = :estado) " +
           "AND (:departamento IS NULL OR LOWER(e.departamentoLab) = LOWER(:departamento))")
    List<Empleado> buscar(@Param("q") String q,
                          @Param("estado") EstadoEmpleado estado,
                          @Param("departamento") String departamento);
}
