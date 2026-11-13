package com.subacol.msauditoria.adapter.in.web;

import com.subacol.msauditoria.application.port.in.HistorialUseCase;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auditoria")
public class HistorialController {
    private final HistorialUseCase historial;

    public HistorialController(HistorialUseCase historial) {
        this.historial = historial;
    }

    @GetMapping("/historial")
    public List<CambioDeEstadoResponse> consultarHistorial(
            @RequestParam(required = false) String ordenId
    ) {
        if (ordenId == null || ordenId.isBlank()) {
            return historial.ultimosCambios().stream()
                    .map(CambioDeEstadoResponse::desde)
                    .toList();
        }
        return historial.historialDeOrden(ordenId).stream()
                .map(CambioDeEstadoResponse::desde)
                .toList();
    }
}
