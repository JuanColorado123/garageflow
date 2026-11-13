# Tareas: 04-avisos-whatsapp

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba`. `<base>` es
  `services/ms-notificaciones/src/main/java/com/subacol/msnotificaciones` y `<test>` es
  `services/ms-notificaciones/src/test/java/com/subacol/msnotificaciones`.
- Cada prueba lleva en `@DisplayName` el spec y el criterio que demuestra (convención de `00-base`).
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-notificaciones: dominio de los avisos
  - Archivos: `<base>/domain/Aviso.java`, `DatosCita.java`, `TipoEvento.java`, `EstadoAviso.java`,
    `MotivoCancelacion.java`, `TipoCita.java`; `<test>/domain/AvisoTest.java`.
  - Hecho cuando: `AvisoTest` pasa y `domain` no importa nada de Spring, `jakarta`, Jackson ni
    Lombok.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-notificaciones services\ms-notificaciones; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.5_

- [ ] 2. ms-notificaciones: caso de uso y puertos
  - Archivos: `<base>/application/port/in/AvisosUseCase.java`;
    `<base>/application/port/out/AvisoRepository.java`, `Notificador.java`;
    `<base>/application/service/AvisosService.java`; `<test>/application/AvisosServiceTest.java`
    (con el repositorio y el notificador falsos dentro de la prueba).
  - Hecho cuando: `AvisosServiceTest` pasa.
  - Validación: la misma de la tarea 1.
  - _Requisitos: REQ-1.1, REQ-1.3, REQ-1.4_

- [ ] 3. ms-notificaciones: consumidores, notificador por log y persistencia
  - Archivos: `<base>/adapter/in/messaging/CitasListener.java`, `CitaAgendadaV1.java`,
    `CitaCanceladaV1.java`; `<base>/adapter/out/notification/WhatsAppLogNotificador.java`;
    `<base>/adapter/out/persistence/AvisoEntity.java`, `AvisoJpaRepository.java`,
    `AvisoPersistenceAdapter.java`; `<base>/config/RabbitConfig.java`
    (2 colas, DLQ y enlaces); `services/ms-notificaciones/db/init.sql`.
  - Hecho cuando: con el sistema arriba, agendar una cita deja una línea `WHATSAPP_SIMULADO` en
    `docker compose logs ms-notificaciones`.
  - Validación: la de la tarea 3 del spec `05-cierre-orden` (`scripts/prueba-cierre.ps1`).
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-1.4, REQ-1.5_

- [ ] 4. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada, y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 1, 2, 3 |
| REQ-1.2 | 1, 3 |
| REQ-1.3 | 2, 3 |
| REQ-1.4 | 2, 3 |
| REQ-1.5 | 1, 3 |

## Cambios

- (sin cambios)
