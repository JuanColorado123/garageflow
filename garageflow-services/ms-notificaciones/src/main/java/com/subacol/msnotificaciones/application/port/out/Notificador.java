package com.subacol.msnotificaciones.application.port.out;

import com.subacol.msnotificaciones.domain.Aviso;

public interface Notificador {
    void enviar(Aviso aviso);
}
