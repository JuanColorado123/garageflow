package com.subacol.msagenda.application.port.out;

import java.util.List;

public interface PublicadorEventos {
    void publicar(List<Object> eventos);
}
