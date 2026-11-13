# Tareas: 02-auditoria

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba`. `<base>` es
  `services/ms-auditoria/src/main/java/com/subacol/msauditoria` y `<test>` es
  `services/ms-auditoria/src/test/java/com/subacol/msauditoria`.
- Cada prueba lleva en `@DisplayName` el criterio que demuestra.
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-auditoria: dominio y caso de uso
  - Archivos: `<base>/domain/CambioDeEstado.java`; `<base>/application/port/in/HistorialUseCase.java`;
    `<base>/application/port/out/HistorialRepository.java`;
    `<base>/application/service/HistorialService.java`; `<test>/domain/CambioDeEstadoTest.java`;
    `<test>/application/HistorialServiceTest.java` (con un repositorio falso dentro de la prueba).
  - Hecho cuando: las dos pruebas pasan dentro de la construcción de la imagen.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-auditoria services\ms-auditoria; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-3.2_

- [ ] 2. ms-auditoria: consumidor, API REST, persistencia y base de datos
  - Archivos: `<base>/adapter/in/messaging/EstadoOrdenCambiadoListener.java`,
    `EstadoOrdenCambiadoV1.java`; `<base>/adapter/in/web/HistorialController.java`,
    `CambioDeEstadoResponse.java`; `<base>/adapter/out/persistence/CambioDeEstadoEntity.java`, `HistorialJpaRepository.java`,
    `HistorialPersistenceAdapter.java`;
    `<base>/config/RabbitConfig.java` (cola, DLQ y enlace); `services/ms-auditoria/db/init.sql`;
    `services/ms-auditoria/db/usuario-app.sh` (con fin de línea LF; el coordinador comprueba que
    no tenga bytes CR); `.gitattributes` en la raíz (`*.sh text eol=lf`, para cuando la carpeta
    sea un repositorio git).
  - Hecho cuando: con el sistema arriba, un cambio de estado de una orden aparece en
    `GET /api/auditoria/historial?ordenId=...` en pocos segundos.
  - Validación: la de la tarea 3.
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-2.1, REQ-2.2, REQ-3.1, REQ-3.2_

- [ ] 3. infraestructura: prueba de auditoría contra el sistema levantado
  - Archivos: `scripts/prueba-auditoria.ps1`. Crea una orden y la avanza 2 veces por la API de
    `ms-ordenes`; espera a que el historial tenga 3 cambios con el usuario correcto y en orden;
    intenta `UPDATE` y `DELETE` como root y como `auditoria_app` con
    `docker compose exec mysql-auditoria mysql ...`; revisa `SHOW GRANTS`; termina con código 0 si
    todo cumple.
  - Hecho cuando: el script termina con `EXIT_CODE_0`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; powershell -ExecutionPolicy Bypass -File .\scripts\prueba-auditoria.ps1; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-2.1, REQ-2.2, REQ-3.1_

- [ ] 4. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada, y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 1, 2, 3 |
| REQ-1.2 | 1, 2 |
| REQ-1.3 | 1, 2 |
| REQ-2.1 | 2, 3 |
| REQ-2.2 | 2, 3 |
| REQ-3.1 | 2, 3 |
| REQ-3.2 | 1, 2 |

## Cambios

- (sin cambios)
