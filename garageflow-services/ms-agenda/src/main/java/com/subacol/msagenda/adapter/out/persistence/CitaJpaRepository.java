package com.subacol.msagenda.adapter.out.persistence;

import com.subacol.msagenda.domain.EstadoCita;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaJpaRepository extends JpaRepository<CitaEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    List<CitaEntity> findByAsesorIdAndFranjaInicioAndEstado(
            Integer asesorId, LocalDateTime franjaInicio, EstadoCita estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CitaEntity c where c.id = :id")
    Optional<CitaEntity> buscarParaActualizar(@Param("id") String id);

    @Query("select c.asesorId from CitaEntity c where c.id = :id")
    Optional<Integer> asesorDeCita(@Param("id") String id);

    @Query(
            "select c.asesorId as asesorId, c.franjaInicio as franjaInicio, count(c) as ocupadas from CitaEntity c where c.estado = :estado and c.franjaInicio >= :desde and c.franjaInicio < :hasta group by c.asesorId, c.franjaInicio")
    List<OcupacionFila> ocupacion(
            @Param("estado") EstadoCita estado,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    List<CitaEntity> findByOrdenIdOrderByFranjaInicioAsc(String ordenId);

    @Query(
            "select distinct c.asesorId from CitaEntity c where c.ordenId = :ordenId and c.estado = :estado order by c.asesorId")
    List<Integer> asesoresConCitas(
            @Param("ordenId") String ordenId, @Param("estado") EstadoCita estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<CitaEntity> findByOrdenIdAndEstado(String ordenId, EstadoCita estado);
}
