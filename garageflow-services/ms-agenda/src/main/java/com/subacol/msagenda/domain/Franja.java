package com.subacol.msagenda.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record Franja(LocalDateTime inicio) {

    public static Franja de(LocalDateTime inicio, LocalDateTime ahora) {
        if (inicio == null
                || inicio.getMinute() != 0
                || inicio.getSecond() != 0
                || inicio.getNano() != 0
                || inicio.getDayOfWeek() == DayOfWeek.SUNDAY
                || inicio.getHour() < 8
                || inicio.getHour() > 16
                || !inicio.isAfter(ahora)) {
            throw new ErrorDeNegocio(
                    ErrorDeNegocio.Tipo.INVALIDO,
                    "FRANJA_INVALIDA",
                    "La franja debe empezar en punto, de lunes a sabado entre las 08:00 y las 16:00, y no puede haber empezado");
        }
        return new Franja(inicio);
    }

    public static List<Franja> delDia(LocalDate fecha, LocalDateTime ahora) {
        List<Franja> franjas = new ArrayList<>();
        for (int hora = 8; hora <= 16; hora++) {
            LocalDateTime inicio = fecha.atTime(hora, 0);
            if (inicio.isAfter(ahora) && inicio.getDayOfWeek() != DayOfWeek.SUNDAY) {
                franjas.add(new Franja(inicio));
            }
        }
        return franjas;
    }
}
