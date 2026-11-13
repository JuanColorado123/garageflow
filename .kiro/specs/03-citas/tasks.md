# Tareas: 03-citas

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba`. `<base>` es
  `services/ms-agenda/src/main/java/com/subacol/msagenda` y `<test>` es
  `services/ms-agenda/src/test/java/com/subacol/msagenda`.
- Cada prueba lleva en `@DisplayName` el spec y el criterio que demuestra (convención de `00-base`).
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-agenda: dominio de franjas, asesores y citas
  - Archivos: `<base>/domain/Franja.java`, `Asesor.java`, `Cita.java`, `TipoCita.java`,
    `EstadoCita.java`, `MotivoCancelacion.java`, `CitaAgendada.java`, `CitaCancelada.java`,
    `Disponibilidad.java`; `<test>/domain/FranjaTest.java`, `AsesorTest.java`, `CitaTest.java`.
  - Hecho cuando: las 3 pruebas pasan y `domain` no importa nada de Spring, `jakarta`, Jackson ni
    Lombok.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-agenda services\ms-agenda; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.2, REQ-1.3, REQ-2.2, REQ-2.4, REQ-3.1, REQ-3.2, REQ-4.2, REQ-5.1, REQ-5.2_

- [ ] 2. ms-agenda: caso de uso y puertos
  - Archivos: `<base>/application/port/in/AgendaUseCase.java`, `AgendarCitaComando.java`;
    `<base>/application/port/out/OrdenesConsulta.java`, `OrdenInfo.java`, `AsesorRepository.java`,
    `CitaRepository.java`, `Ocupacion.java`, `PublicadorEventos.java`;
    `<base>/application/service/AgendaService.java`; `<test>/application/AgendaServiceTest.java`
    (con los puertos falsos dentro de la prueba).
  - Hecho cuando: `AgendaServiceTest` pasa.
  - Validación: la misma de la tarea 1.
  - _Requisitos: REQ-1.2, REQ-2.1, REQ-2.2, REQ-2.3, REQ-2.5, REQ-2.6, REQ-2.7, REQ-2.8, REQ-3.1, REQ-3.3, REQ-5.3_

- [ ] 3. ms-agenda: API REST y cliente de ms-ordenes
  - Archivos: `<base>/adapter/in/web/AgendaController.java`, `AgendarCitaRequest.java`,
    `CitaResponse.java`, `AsesorResponse.java`, `DisponibilidadResponse.java`;
    `<base>/adapter/out/http/OrdenesRestClient.java`, `OrdenRemotaV1.java`;
    `services/ms-agenda/src/main/resources/application.properties` (`app.ordenes.url`).
  - Hecho cuando: con el sistema arriba, `POST /api/agenda/citas` crea una cita para una orden de
    `ms-ordenes` y devuelve los rechazos de la tabla de contratos.
  - Validación: la de la tarea 5.
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-2.1, REQ-2.5, REQ-2.6, REQ-2.8, REQ-2.9, REQ-3.1, REQ-4.1, REQ-4.2_

- [ ] 4. ms-agenda: persistencia, outbox y base de datos
  - Archivos: `<base>/adapter/out/persistence/AsesorEntity.java`, `CitaEntity.java`,
    `AsesorJpaRepository.java`, `CitaJpaRepository.java`, `OcupacionFila.java`,
    `AsesorPersistenceAdapter.java`, `CitaPersistenceAdapter.java`;
    `<base>/adapter/out/messaging/OutboxEntity.java`, `OutboxJpaRepository.java`,
    `OutboxPublicador.java`, `OutboxRelay.java`, `CitaAgendadaV1.java`, `CitaCanceladaV1.java`;
    `services/ms-agenda/db/init.sql` (asesores con sus datos iniciales, citas y outbox).
  - Hecho cuando: con el sistema arriba, una cita agendada y una cancelada publican sus eventos en
    `taller.eventos`.
  - Validación: la de la tarea 5.
  - _Requisitos: REQ-1.1, REQ-2.3, REQ-3.1, REQ-4.1, REQ-5.1, REQ-5.2, REQ-5.3_

- [ ] 5. infraestructura: prueba de cupo contra el sistema levantado
  - Archivos: `scripts/prueba-cupo.ps1`. Lee los asesores; crea una orden en `ms-ordenes`; busca
    la próxima franja válida; envía 6 reservas simultáneas para el asesor 1 y cuenta 2 creadas y
    4 `SIN_CUPO`; revisa que la disponibilidad muestre 0 libres; cancela una y revisa 1 libre;
    prueba `FRANJA_INVALIDA` (10:30) y `DATOS_INVALIDOS` (tipo `LAVAR`); lista las citas de la
    orden; termina con código 0 si todo cumple.
  - Hecho cuando: el script termina con `EXIT_CODE_0`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; powershell -ExecutionPolicy Bypass -File .\scripts\prueba-cupo.ps1; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-2.3, REQ-2.4, REQ-2.9, REQ-3.1, REQ-4.1_

- [ ] 6. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada (incluida la de REQ-2.8 con `ms-ordenes` detenido), y la revisión de
    trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 3, 4, 5 |
| REQ-1.2 | 1, 2, 3 |
| REQ-1.3 | 1 |
| REQ-2.1 | 2, 3 |
| REQ-2.2 | 1, 2 |
| REQ-2.3 | 2, 4, 5 |
| REQ-2.4 | 1, 5 |
| REQ-2.5 | 2, 3 |
| REQ-2.6 | 2, 3 |
| REQ-2.7 | 2 |
| REQ-2.8 | 2, 3 |
| REQ-2.9 | 3, 5 |
| REQ-3.1 | 1, 2, 3, 4, 5 |
| REQ-3.2 | 1 |
| REQ-3.3 | 2 |
| REQ-4.1 | 3, 4, 5 |
| REQ-4.2 | 1, 3 |
| REQ-5.1 | 1, 4 |
| REQ-5.2 | 1, 4 |
| REQ-5.3 | 2, 4 |

## Cambios

- (sin cambios)
