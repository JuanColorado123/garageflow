package com.subacol.msordenes.application.port.in;

import com.subacol.msordenes.domain.Orden;
import java.util.List;

public interface OrdenesUseCase {
    Orden registrar(RegistrarOrdenComando comando);
    Orden avanzar(String id, String usuario);
    Orden reabrir(String id, String motivo, String usuario);
    Orden obtener(String id);
    List<Orden> listar();
}
