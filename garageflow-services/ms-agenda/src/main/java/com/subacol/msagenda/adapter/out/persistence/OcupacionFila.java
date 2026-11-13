package com.subacol.msagenda.adapter.out.persistence;

import java.time.LocalDateTime;

public interface OcupacionFila {
    Integer getAsesorId();

    LocalDateTime getFranjaInicio();

    Long getOcupadas();
}
