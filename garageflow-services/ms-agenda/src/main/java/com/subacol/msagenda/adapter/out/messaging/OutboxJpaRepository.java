package com.subacol.msagenda.adapter.out.messaging;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, String> {
    List<OutboxEntity> findTop50ByPublicadoEnIsNullOrderByCreadoEnAsc();
}
