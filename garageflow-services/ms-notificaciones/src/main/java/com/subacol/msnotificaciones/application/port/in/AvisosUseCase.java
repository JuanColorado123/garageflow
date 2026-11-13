package com.subacol.msnotificaciones.application.port.in;

import com.subacol.msnotificaciones.domain.DatosCita;
import com.subacol.msnotificaciones.domain.MotivoCancelacion;

public interface AvisosUseCase {
    void citaAgendada(String eventoId, DatosCita cita);
    void citaCancelada(String eventoId, DatosCita cita, MotivoCancelacion motivo);
}
