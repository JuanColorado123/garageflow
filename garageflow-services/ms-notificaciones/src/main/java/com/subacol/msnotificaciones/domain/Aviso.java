package com.subacol.msnotificaciones.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Aviso(
        String eventoId,
        String citaId,
        String ordenId,
        TipoEvento tipoEvento,
        String telefono,
        String mensaje,
        EstadoAviso estado
) {
    private static final DateTimeFormatter FECHA_FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_FORMATO = DateTimeFormatter.ofPattern("HH:mm");

    public static Aviso porCitaAgendada(String eventoId, DatosCita cita) {
        if (eventoId == null || eventoId.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento le falta eventoId");
        }
        if (cita.tipoCita() == null) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de la cita le falta tipoCita");
        }

        String fecha = cita.franjaInicio().format(FECHA_FORMATO);
        String hora = cita.franjaInicio().format(HORA_FORMATO);
        String mensaje = String.format(
                "Hola %s, tu cita para %s tu vehículo quedó agendada el %s a las %s.",
                cita.clienteNombre(),
                cita.tipoCita().texto(),
                fecha,
                hora
        );

        return new Aviso(
                eventoId,
                cita.citaId(),
                cita.ordenId(),
                TipoEvento.CITA_AGENDADA,
                cita.clienteTelefono(),
                mensaje,
                EstadoAviso.ENVIADO
        );
    }

    public static Aviso porCitaCancelada(String eventoId, DatosCita cita, MotivoCancelacion motivo) {
        if (eventoId == null || eventoId.isBlank()) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento le falta eventoId");
        }
        if (motivo == null) {
            throw new ErrorDeNegocio(ErrorDeNegocio.Tipo.INVALIDO, "EVENTO_INVALIDO", "Al evento de cancelación le falta motivo");
        }

        String mensaje = null;
        EstadoAviso estado = EstadoAviso.ENVIADO;

        if (motivo == MotivoCancelacion.MANUAL) {
            String fecha = cita.franjaInicio().format(FECHA_FORMATO);
            String hora = cita.franjaInicio().format(HORA_FORMATO);
            mensaje = String.format(
                    "Hola %s, tu cita del %s a las %s fue cancelada.",
                    cita.clienteNombre(),
                    fecha,
                    hora
            );
        } else if (motivo == MotivoCancelacion.CIERRE_ORDEN) {
            estado = EstadoAviso.OMITIDO;
        }

        return new Aviso(
                eventoId,
                cita.citaId(),
                cita.ordenId(),
                TipoEvento.CITA_CANCELADA,
                cita.clienteTelefono(),
                mensaje,
                estado
        );
    }

    public boolean debeEnviarse() {
        return estado == EstadoAviso.ENVIADO;
    }
}
