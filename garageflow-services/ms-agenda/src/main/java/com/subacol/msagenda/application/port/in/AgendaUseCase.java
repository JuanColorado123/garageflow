package com.subacol.msagenda.application.port.in;

import com.subacol.msagenda.domain.Asesor;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.Disponibilidad;
import java.time.LocalDate;
import java.util.List;

public interface AgendaUseCase {
    List<Asesor> asesores();

    List<Disponibilidad> disponibilidad(LocalDate fecha);

    Cita agendar(AgendarCitaComando c);

    Cita cancelar(String citaId);

    List<Cita> citasDeOrden(String ordenId);

    void alCerrarseOrden(String eventoId, String ordenId);
}
