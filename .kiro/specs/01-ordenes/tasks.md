# Tareas: 01-ordenes

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba`. `<base>` es
  `services/ms-ordenes/src/main/java/com/subacol/msordenes` y `<test>` es
  `services/ms-ordenes/src/test/java/com/subacol/msordenes`.
- Cada prueba lleva en `@DisplayName` el criterio que demuestra.
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-ordenes: dominio de la orden
  - Archivos: `<base>/domain/Orden.java`, `DatosOrden.java`, `EstadoOrden.java`, `Origen.java`,
    `AccionOrden.java`, `EstadoOrdenCambiado.java`, `OrdenCerrada.java`; `<test>/domain/OrdenTest.java`.
  - Hecho cuando: `OrdenTest` pasa y `domain` no importa nada de Spring, `jakarta`, Jackson ni Lombok.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-ordenes services\ms-ordenes; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-2.1, REQ-2.2, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1, REQ-5.2, REQ-6.1, REQ-6.2_

- [ ] 2. ms-ordenes: caso de uso y puertos
  - Archivos: `<base>/application/port/in/OrdenesUseCase.java`, `RegistrarOrdenComando.java`;
    `<base>/application/port/out/OrdenRepository.java`, `PublicadorEventos.java`;
    `<base>/application/service/OrdenesService.java`; `<test>/application/OrdenesServiceTest.java`
    (con el repositorio y el publicador falsos dentro de la prueba).
  - Hecho cuando: `OrdenesServiceTest` pasa.
  - Validación: la misma de la tarea 1.
  - _Requisitos: REQ-1.1, REQ-2.3, REQ-2.4, REQ-6.3, REQ-7.2_

- [ ] 3. ms-ordenes: API REST, persistencia y outbox
  - Archivos: `<base>/adapter/in/web/OrdenesController.java`, `RegistrarOrdenRequest.java`,
    `ReabrirOrdenRequest.java`, `OrdenResponse.java`;
    `<base>/adapter/out/persistence/OrdenEntity.java`, `OrdenJpaRepository.java`,
    `OrdenPersistenceAdapter.java`; `<base>/adapter/out/messaging/OutboxEntity.java`,
    `OutboxJpaRepository.java`;
    `<base>/adapter/out/messaging/OutboxPublicador.java`, `OutboxRelay.java`,
    `EstadoOrdenCambiadoV1.java`, `OrdenCerradaV1.java`; `services/ms-ordenes/db/init.sql`.
  - Hecho cuando: con el sistema arriba, una orden creada por la API aparece en la lista y sus
    eventos se publican en `taller.eventos`. REQ-6.4 (RabbitMQ caído) se revisa a mano en la
    tarea 5.
  - Validación: la de la tarea 4.
  - _Requisitos: REQ-1.2, REQ-2.4, REQ-4.1, REQ-5.1, REQ-5.2, REQ-6.1, REQ-6.2, REQ-6.3, REQ-6.4, REQ-7.1_

- [ ] 4. infraestructura: prueba de órdenes contra el sistema levantado
  - Archivos: `scripts/prueba-ordenes.ps1`. Registra una orden, prueba los rechazos
    (`DATOS_INVALIDOS`, `USUARIO_REQUERIDO`, `TRANSICION_INVALIDA`, `MOTIVO_REQUERIDO`), avanza hasta
    `CERRADA` revisando `siguienteEstado` y `accionesPermitidas` en cada paso, reabre con motivo,
    revisa el orden de la lista y termina con código 0 si todo cumple.
  - Hecho cuando: el script termina con `EXIT_CODE_0`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; powershell -ExecutionPolicy Bypass -File .\scripts\prueba-ordenes.ps1; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-2.1, REQ-2.2, REQ-3.1, REQ-3.2, REQ-4.1, REQ-5.1, REQ-5.2, REQ-7.1_

- [ ] 5. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada (incluida la de REQ-6.4), y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 1, 2, 4 |
| REQ-1.2 | 1, 3, 4 |
| REQ-1.3 | 1 |
| REQ-2.1 | 1, 4 |
| REQ-2.2 | 1, 4 |
| REQ-2.3 | 2 |
| REQ-2.4 | 2, 3 |
| REQ-3.1 | 1, 4 |
| REQ-3.2 | 1, 4 |
| REQ-3.3 | 1 |
| REQ-4.1 | 1, 3, 4 |
| REQ-5.1 | 3, 4 |
| REQ-5.2 | 1, 3, 4 |
| REQ-6.1 | 1, 3 |
| REQ-6.2 | 1, 3 |
| REQ-6.3 | 2, 3 |
| REQ-6.4 | 3 |
| REQ-7.1 | 3, 4 |
| REQ-7.2 | 2 |

## Cambios

- (sin cambios)
