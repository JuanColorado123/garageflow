package com.subacol.msordenes.application.port.out;

import com.subacol.msordenes.domain.Orden;
import java.util.List;
import java.util.Optional;

public interface OrdenRepository {
    void guardarNueva(Orden orden);
    Optional<Orden> buscar(String id);
    List<Orden> listar();
    boolean actualizar(Orden orden);
}
