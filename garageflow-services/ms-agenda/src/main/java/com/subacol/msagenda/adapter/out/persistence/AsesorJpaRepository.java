package com.subacol.msagenda.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsesorJpaRepository extends JpaRepository<AsesorEntity, Integer> {
    List<AsesorEntity> findAllByOrderByIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AsesorEntity a where a.id = :id")
    Optional<AsesorEntity> buscarParaActualizar(@Param("id") Integer id);
}
