package com.subacol.msordenes.adapter.in.web;

import com.subacol.msordenes.application.port.in.OrdenesUseCase;
import com.subacol.msordenes.application.port.in.RegistrarOrdenComando;
import com.subacol.msordenes.domain.DatosOrden;
import com.subacol.msordenes.domain.Orden;
import com.subacol.msordenes.domain.Origen;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenesController {
    private final OrdenesUseCase useCase;

    public OrdenesController(OrdenesUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponse registrar(
        @RequestHeader(value = "X-Usuario", required = false) String usuario,
        @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol,
        @Valid @RequestBody RegistrarOrdenRequest req
    ) {
        DatosOrden datos = new DatosOrden(
            req.clienteNombre(),
            req.clienteTelefono(),
            req.vehiculoPlaca(),
            req.vehiculoDescripcion(),
            req.aseguradora(),
            req.trabajo()
        );
        RegistrarOrdenComando comando = new RegistrarOrdenComando(datos, Origen.ASESOR, usuario);
        Orden orden = useCase.registrar(comando);
        return OrdenResponse.desde(orden, rol.puedeModificar());
    }

    @GetMapping
    public List<OrdenResponse> listar(
        @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol
    ) {
        return useCase.listar().stream()
            .map(orden -> OrdenResponse.desde(orden, rol.puedeModificar()))
            .toList();
    }

    @GetMapping("/{id}")
    public OrdenResponse obtener(
        @PathVariable String id,
        @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol
    ) {
        Orden orden = useCase.obtener(id);
        return OrdenResponse.desde(orden, rol.puedeModificar());
    }

    @PostMapping("/{id}/avanzar")
    public OrdenResponse avanzar(
        @PathVariable String id,
        @RequestHeader(value = "X-Usuario", required = false) String usuario,
        @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol
    ) {
        Orden orden = useCase.avanzar(id, usuario);
        return OrdenResponse.desde(orden, rol.puedeModificar());
    }

    @PostMapping("/{id}/reabrir")
    public OrdenResponse reabrir(
        @PathVariable String id,
        @RequestHeader(value = "X-Usuario", required = false) String usuario,
        @RequestAttribute(RolInterceptor.ATRIBUTO) Rol rol,
        @Valid @RequestBody(required = false) ReabrirOrdenRequest req
    ) {
        String motivo = req == null ? null : req.motivo();
        Orden orden = useCase.reabrir(id, motivo, usuario);
        return OrdenResponse.desde(orden, rol.puedeModificar());
    }
}
