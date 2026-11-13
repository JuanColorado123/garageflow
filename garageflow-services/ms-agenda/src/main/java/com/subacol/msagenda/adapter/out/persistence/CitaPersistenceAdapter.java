package com.subacol.msagenda.adapter.out.persistence;

import com.subacol.msagenda.application.port.out.CitaRepository;
import com.subacol.msagenda.application.port.out.Ocupacion;
import com.subacol.msagenda.domain.Cita;
import com.subacol.msagenda.domain.EstadoCita;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CitaPersistenceAdapter implements CitaRepository {
    private final CitaJpaRepository repository;

    public CitaPersistenceAdapter(CitaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void guardar(Cita c) {
        CitaEntity entity = toEntity(c);
        repository.save(entity);
    }

    @Override
    public Optional<Integer> asesorDeCita(String citaId) {
        return repository.asesorDeCita(citaId);
    }

    @Override
    public Optional<Cita> buscarParaActualizar(String citaId) {
        return repository.buscarParaActualizar(citaId).map(this::toDomain);
    }

    @Override
    public List<Cita> porOrden(String ordenId) {
        return repository.findByOrdenIdOrderByFranjaInicioAsc(ordenId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int contarPendientes(Integer asesorId, LocalDateTime franjaInicio) {
        return repository
                .findByAsesorIdAndFranjaInicioAndEstado(
                        asesorId, franjaInicio, EstadoCita.PENDIENTE)
                .size();
    }

    @Override
    public List<Ocupacion> ocupacionDelDia(LocalDate fecha) {
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();
        return repository.ocupacion(EstadoCita.PENDIENTE, desde, hasta).stream()
                .map(o -> new Ocupacion(o.getAsesorId(), o.getFranjaInicio(), o.getOcupadas().intValue()))
                .toList();
    }

    @Override
    public List<Integer> asesoresConPendientes(String ordenId) {
        return repository.asesoresConCitas(ordenId, EstadoCita.PENDIENTE);
    }

    @Override
    public List<Cita> pendientesDeOrdenParaActualizar(String ordenId) {
        return repository.findByOrdenIdAndEstado(ordenId, EstadoCita.PENDIENTE).stream()
                .map(this::toDomain)
                .toList();
    }

    private Cita toDomain(CitaEntity entity) {
        return Cita.reconstituir(
                entity.getId(),
                entity.getOrdenId(),
                entity.getAsesorId(),
                entity.getFranjaInicio(),
                entity.getTipo(),
                entity.getEstado(),
                entity.getMotivoCancelacion(),
                entity.getClienteNombre(),
                entity.getClienteTelefono(),
                entity.getCreadaEn(),
                entity.getCanceladaEn());
    }

    private CitaEntity toEntity(Cita cita) {
        return new CitaEntity(
                cita.id(),
                cita.ordenId(),
                cita.asesorId(),
                cita.franjaInicio(),
                cita.tipo(),
                cita.estado(),
                cita.motivoCancelacion(),
                cita.clienteNombre(),
                cita.clienteTelefono(),
                cita.creadaEn(),
                cita.canceladaEn());
    }
}
