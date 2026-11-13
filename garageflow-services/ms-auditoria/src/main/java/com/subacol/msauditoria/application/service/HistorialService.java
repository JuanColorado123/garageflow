package com.subacol.msauditoria.application.service;

import com.subacol.msauditoria.application.port.in.HistorialUseCase;
import com.subacol.msauditoria.application.port.out.HistorialRepository;
import com.subacol.msauditoria.domain.CambioDeEstado;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistorialService implements HistorialUseCase {
    private final HistorialRepository repository;

    public HistorialService(HistorialRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public boolean registrar(CambioDeEstado c) {
        return repository.agregar(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CambioDeEstado> historialDeOrden(String ordenId) {
        return repository.porOrden(ordenId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CambioDeEstado> ultimosCambios() {
        return repository.ultimos(200);
    }
}
