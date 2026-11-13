package com.subacol.msordenes.application.port.out;

import java.util.List;

public interface PublicadorEventos {
    void publicar(List<Object> eventos);
}
