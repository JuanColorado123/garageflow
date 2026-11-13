package com.subacol.msagenda.application.port.out;

import java.util.Optional;

public interface OrdenesConsulta {
    Optional<OrdenInfo> obtener(String ordenId);
}
