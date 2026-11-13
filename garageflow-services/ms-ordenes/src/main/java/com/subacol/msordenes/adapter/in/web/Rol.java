package com.subacol.msordenes.adapter.in.web;

import java.util.Locale;
import java.util.Optional;

public enum Rol {
    ASESOR, AUDITOR, ADMIN;

    public static Optional<Rol> desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Rol.valueOf(valor.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean puedeModificar() {
        return this == ASESOR || this == ADMIN;
    }
}
