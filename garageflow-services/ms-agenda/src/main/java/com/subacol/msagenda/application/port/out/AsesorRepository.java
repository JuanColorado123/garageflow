package com.subacol.msagenda.application.port.out;

import com.subacol.msagenda.domain.Asesor;
import java.util.List;
import java.util.Optional;

public interface AsesorRepository {
    List<Asesor> listar();

    Optional<Asesor> buscarYBloquear(Integer id);
}
