# Tareas: 05-cierre-orden

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba`. `<agenda>` es
  `services/ms-agenda/src/main/java/com/subacol/msagenda`, `<agenda-test>` es
  `services/ms-agenda/src/test/java/com/subacol/msagenda`, `<notif>` es
  `services/ms-notificaciones/src/main/java/com/subacol/msnotificaciones` y `<notif-test>` es
  `services/ms-notificaciones/src/test/java/com/subacol/msnotificaciones`.
- Cada prueba lleva en `@DisplayName` el spec y el criterio que demuestra (convención de `00-base`).
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-agenda: cancelar las citas al cerrarse la orden
  - Archivos: `<agenda>/adapter/in/messaging/OrdenCerradaListener.java`, `OrdenCerradaV1.java`;
    `<agenda>/application/port/in/AgendaUseCase.java`;
    `<agenda>/application/port/out/EventosProcesados.java`, `CitaRepository.java`;
    `<agenda>/application/service/AgendaService.java`;
    `<agenda>/adapter/out/persistence/EventoProcesadoEntity.java`,
    `EventoProcesadoJpaRepository.java`, `EventosProcesadosPersistenceAdapter.java`,
    `CitaJpaRepository.java`, `CitaPersistenceAdapter.java`; `<agenda>/config/RabbitConfig.java`;
    `services/ms-agenda/db/init.sql`; `<agenda-test>/application/AgendaServiceTest.java`.
  - Hecho cuando: las pruebas de este spec en `AgendaServiceTest` pasan.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-agenda services\ms-agenda; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-1.4_

- [ ] 2. ms-notificaciones: omitir el aviso cuando la cancelación es por cierre
  - Archivos: `<notif>/domain/Aviso.java`, `<notif>/application/service/AvisosService.java`,
    `<notif-test>/domain/AvisoTest.java`, `<notif-test>/application/AvisosServiceTest.java`.
  - Hecho cuando: las pruebas de este spec en `AvisoTest` y `AvisosServiceTest` pasan.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-notificaciones services\ms-notificaciones; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-2.1, REQ-2.2_

- [ ] 3. infraestructura: prueba de cierre contra el sistema levantado
  - Archivos: `scripts/prueba-cierre.ps1`. Crea una orden y la avanza hasta `REPARADA`; agenda las
    citas A y B; cancela B a mano; revisa en `docker compose logs ms-notificaciones` el
    `WHATSAPP_SIMULADO` de A (agendada) y los 2 de B (agendada y cancelada); avanza la orden a
    `CERRADA`; espera hasta 20 s a que A quede `CANCELADA` con `CIERRE_ORDEN`; revisa que B siga
    con `MANUAL`, que haya un `WHATSAPP_OMITIDO cita=<A>` y una sola línea `WHATSAPP_SIMULADO` de
    A; termina con código 0 si todo cumple. También deja la evidencia de REQ-1.1 y REQ-1.2 del
    spec `04-avisos-whatsapp`.
  - Hecho cuando: el script termina con `EXIT_CODE_0`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; powershell -ExecutionPolicy Bypass -File .\scripts\prueba-cierre.ps1; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.4, REQ-2.1, REQ-2.2_

- [ ] 4. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada, y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 1, 3 |
| REQ-1.2 | 1, 3 |
| REQ-1.3 | 1 |
| REQ-1.4 | 1, 3 |
| REQ-2.1 | 2, 3 |
| REQ-2.2 | 2, 3 |

## Cambios

- (sin cambios)
