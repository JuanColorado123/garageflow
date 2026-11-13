package com.subacol.msordenes.domain;

public enum EstadoOrden {
    REGISTRADA,
    EN_DIAGNOSTICO,
    EN_REPARACION,
    REPARADA,
    CERRADA;

    public EstadoOrden siguiente() {
        return switch (this) {
            case REGISTRADA -> EN_DIAGNOSTICO;
            case EN_DIAGNOSTICO -> EN_REPARACION;
            case EN_REPARACION -> REPARADA;
            case REPARADA -> CERRADA;
            case CERRADA -> null;
        };
    }
}
