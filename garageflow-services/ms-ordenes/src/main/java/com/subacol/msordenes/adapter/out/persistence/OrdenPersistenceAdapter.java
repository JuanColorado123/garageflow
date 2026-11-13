package com.subacol.msordenes.adapter.out.persistence;

import com.subacol.msordenes.application.port.out.OrdenRepository;
import com.subacol.msordenes.domain.DatosOrden;
import com.subacol.msordenes.domain.Orden;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrdenPersistenceAdapter implements OrdenRepository {
    private final OrdenJpaRepository jpa;

    public OrdenPersistenceAdapter(OrdenJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardarNueva(Orden orden) {
        OrdenEntity entity = fromDomain(orden);
        jpa.save(entity);
    }

    @Override
    public Optional<Orden> buscar(String id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Orden> listar() {
        return jpa.findAllByOrderByCreadaEnDesc().stream()
            .map(this::toDomain)
            .toList();
    }

    @Override
    public boolean actualizar(Orden orden) {
        try {
            OrdenEntity entity = fromDomain(orden);
            jpa.saveAndFlush(entity);
            return true;
        } catch (OptimisticLockingFailureException e) {
            return false;
        }
    }

    private OrdenEntity fromDomain(Orden orden) {
        return new OrdenEntity(
            orden.id(),
            orden.datos().clienteNombre(),
            orden.datos().clienteTelefono(),
            orden.datos().vehiculoPlaca(),
            orden.datos().vehiculoDescripcion(),
            orden.datos().aseguradora(),
            orden.datos().trabajo(),
            orden.origen(),
            orden.estado(),
            orden.creadaEn(),
            orden.actualizadaEn(),
            orden.version()
        );
    }

    private Orden toDomain(OrdenEntity entity) {
        DatosOrden datos = new DatosOrden(
            entity.getClienteNombre(),
            entity.getClienteTelefono(),
            entity.getVehiculoPlaca(),
            entity.getVehiculoDescripcion(),
            entity.getAseguradora(),
            entity.getTrabajo()
        );
        return Orden.reconstituir(
            entity.getId(),
            datos,
            entity.getOrigen(),
            entity.getEstado(),
            entity.getCreadaEn(),
            entity.getActualizadaEn(),
            entity.getVersion()
        );
    }
}
