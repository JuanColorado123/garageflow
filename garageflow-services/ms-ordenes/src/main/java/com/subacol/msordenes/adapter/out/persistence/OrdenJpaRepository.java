package com.subacol.msordenes.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenJpaRepository extends JpaRepository<OrdenEntity, String> {
    List<OrdenEntity> findAllByOrderByCreadaEnDesc();
}
