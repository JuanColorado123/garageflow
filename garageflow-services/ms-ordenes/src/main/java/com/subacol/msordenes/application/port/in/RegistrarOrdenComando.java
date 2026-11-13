package com.subacol.msordenes.application.port.in;

import com.subacol.msordenes.domain.DatosOrden;
import com.subacol.msordenes.domain.Origen;

public record RegistrarOrdenComando(
    DatosOrden datos,
    Origen origen,
    String usuario
) {
}
