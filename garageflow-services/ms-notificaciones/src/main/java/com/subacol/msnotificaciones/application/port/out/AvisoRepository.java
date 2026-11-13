package com.subacol.msnotificaciones.application.port.out;

import com.subacol.msnotificaciones.domain.Aviso;

public interface AvisoRepository {
    boolean registrarSiEsNuevo(Aviso aviso);
}
