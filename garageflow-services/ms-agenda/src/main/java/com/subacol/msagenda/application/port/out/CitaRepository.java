package com.subacol.msagenda.application.port.out;

import com.subacol.msagenda.domain.Cita;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CitaRepository {
    void guardar(Cita c);

    Optional<Integer> asesorDeCita(String citaId);

    Optional<Cita> buscarParaActualizar(String citaId);

    List<Cita> porOrden(String ordenId);

    int contarPendientes(Integer asesorId, java.time.LocalDateTime franjaInicio);

    List<Ocupacion> ocupacionDelDia(LocalDate fecha);

    List<Integer> asesoresConPendientes(String ordenId);

    List<Cita> pendientesDeOrdenParaActualizar(String ordenId);
}
