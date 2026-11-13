package com.subacol.msordenes.adapter.out.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, String> {
    List<OutboxEntity> findTop50ByPublicadoEnIsNullOrderByCreadoEnAsc();
}
