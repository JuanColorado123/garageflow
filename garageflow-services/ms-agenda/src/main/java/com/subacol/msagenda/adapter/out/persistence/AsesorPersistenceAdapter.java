package com.subacol.msagenda.adapter.out.persistence;

import com.subacol.msagenda.application.port.out.AsesorRepository;
import com.subacol.msagenda.domain.Asesor;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class AsesorPersistenceAdapter implements AsesorRepository {
    private final AsesorJpaRepository repository;

    public AsesorPersistenceAdapter(AsesorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Asesor> listar() {
        return repository.findAllByOrderByIdAsc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Asesor> buscarYBloquear(Integer id) {
        return repository.buscarParaActualizar(id).map(this::toDomain);
    }

    private Asesor toDomain(AsesorEntity entity) {
        return new Asesor(entity.getId(), entity.getNombre(), entity.getCupoPorFranja());
    }
}
