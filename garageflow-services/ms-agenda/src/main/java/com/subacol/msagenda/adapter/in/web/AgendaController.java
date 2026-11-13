package com.subacol.msagenda.adapter.in.web;

import com.subacol.msagenda.application.port.in.AgendarCitaComando;
import com.subacol.msagenda.application.port.in.AgendaUseCase;
import com.subacol.msagenda.domain.Disponibilidad;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agenda")
public class AgendaController {
    private final AgendaUseCase agenda;

    public AgendaController(AgendaUseCase agenda) {
        this.agenda = agenda;
    }

    @GetMapping("/asesores")
    public List<AsesorResponse> asesores() {
        return agenda.asesores().stream()
                .map(a -> new AsesorResponse(a.id(), a.nombre(), a.cupoPorFranja()))
                .toList();
    }

    @GetMapping("/disponibilidad")
    public List<DisponibilidadResponse> disponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return agenda.disponibilidad(fecha).stream()
                .map(d -> new DisponibilidadResponse(
                                        d.asesorId(),
                                        d.asesorNombre(),
                                        d.franjaInicio(),
                                        d.cupo(),
                                        d.ocupadas(),
                                        d.libres()))
                .toList();
    }

    @PostMapping("/citas")
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse agendar(
            @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol,
            @Valid @RequestBody AgendarCitaRequest request) {var cita =
                agenda.agendar(new AgendarCitaComando(
                                request.ordenId(),
                                request.asesorId(),
                                request.franjaInicio(),
                                request.tipo()));
        return CitaResponse.desde(cita, rol.puedeModificar());
    }

    @GetMapping("/citas")
    public List<CitaResponse> citasDeOrden(
            @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol,
            @RequestParam String ordenId) {
        return agenda.citasDeOrden(ordenId).stream()
                .map(c -> CitaResponse.desde(c, rol.puedeModificar()))
                .toList();
    }

    @PostMapping("/citas/{id}/cancelar")
    public CitaResponse cancelar(
            @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol, @PathVariable String id) {
        var cita = agenda.cancelar(id);
        return CitaResponse.desde(cita, rol.puedeModificar());
    }
}
