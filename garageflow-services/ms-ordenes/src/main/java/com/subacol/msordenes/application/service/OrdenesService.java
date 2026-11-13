package com.subacol.msordenes.application.service;

import com.subacol.msordenes.application.port.in.OrdenesUseCase;
import com.subacol.msordenes.application.port.in.RegistrarOrdenComando;
import com.subacol.msordenes.application.port.out.OrdenRepository;
import com.subacol.msordenes.application.port.out.PublicadorEventos;
import com.subacol.msordenes.domain.ErrorDeNegocio;
import com.subacol.msordenes.domain.Orden;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenesService implements OrdenesUseCase {
    private final OrdenRepository repository;
    private final PublicadorEventos publicador;
    private final Clock clock;

    public OrdenesService(OrdenRepository repository, PublicadorEventos publicador, Clock clock) {
        this.repository = repository;
        this.publicador = publicador;
        this.clock = clock;
    }

    @Transactional
    @Override
    public Orden registrar(RegistrarOrdenComando comando) {
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
        Orden orden = Orden.registrar(
            UUID.randomUUID().toString(),
            comando.datos(),
            comando.origen(),
            comando.usuario(),
            ahora
        );
        repository.guardarNueva(orden);
        publicador.publicar(orden.extraerEventos());
        return orden;
    }

    @Transactional
    @Override
    public Orden avanzar(String id, String usuario) {
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
        Orden orden = repository.buscar(id)
            .orElseThrow(() -> new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                "ORDEN_NO_ENCONTRADA",
                "No existe la orden " + id
            ));

        orden.avanzar(usuario, ahora);

        if (!repository.actualizar(orden)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.CONFLICTO,
                "ORDEN_MODIFICADA",
                "Otro usuario cambió la orden; vuelva a cargarla"
            );
        }

        publicador.publicar(orden.extraerEventos());
        return orden;
    }

    @Transactional
    @Override
    public Orden reabrir(String id, String motivo, String usuario) {
        LocalDateTime ahora = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
        Orden orden = repository.buscar(id)
            .orElseThrow(() -> new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                "ORDEN_NO_ENCONTRADA",
                "No existe la orden " + id
            ));

        orden.reabrir(motivo, usuario, ahora);

        if (!repository.actualizar(orden)) {
            throw new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.CONFLICTO,
                "ORDEN_MODIFICADA",
                "Otro usuario cambió la orden; vuelva a cargarla"
            );
        }

        publicador.publicar(orden.extraerEventos());
        return orden;
    }

    @Transactional(readOnly = true)
    @Override
    public Orden obtener(String id) {
        return repository.buscar(id)
            .orElseThrow(() -> new ErrorDeNegocio(
                ErrorDeNegocio.Tipo.NO_ENCONTRADO,
                "ORDEN_NO_ENCONTRADA",
                "No existe la orden " + id
            ));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Orden> listar() {
        return repository.listar();
    }
}
