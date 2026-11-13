# Tareas: 00-base

Estado: APROBADO (2026-11-12, Developer)

- Cada tarea toca un solo proyecto: un microservicio, el frontend o la infraestructura.
- Las rutas son relativas a `c:\repos_cobis\Prueba`. `<base>` es la carpeta del paquete base del
  microservicio (por ejemplo `services/ms-ordenes/src/main/java/com/subacol/msordenes`).
- Solo el coordinador marca `[x]`, después de la revisión aprobada y con la evidencia del
  Developer. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. ms-ordenes: base del proyecto
  - Archivos: `services/ms-ordenes/pom.xml`, `src/main/resources/application.properties`,
    `Dockerfile`, `.dockerignore`, `db/init.sql` (encabezado), `<base>/domain/ErrorDeNegocio.java`,
    `<base>/adapter/in/web/ManejadorErrores.java`, `<base>/config/AppConfig.java`,
    `<base>/config/CorsConfig.java`, `<base>/config/RabbitConfig.java`. Elimina
    `src/test/java/com/subacol/msordenes/MsOrdenesApplicationTests.java`.
  - Hecho cuando: la imagen se construye y, con el sistema arriba, `GET /actuator/health` en el
    puerto 8081 responde `UP`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-ordenes services\ms-ordenes; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.2, REQ-1.3, REQ-1.6, REQ-2.1, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_

- [ ] 2. ms-agenda: base del proyecto (Gradle)
  - Archivos: `services/ms-agenda/build.gradle` (dependencias y `jar { enabled = false }`),
    `src/main/resources/application.properties`, `Dockerfile`, `.dockerignore`, `db/init.sql`
    (encabezado), `<base>/domain/ErrorDeNegocio.java`, `<base>/adapter/in/web/ManejadorErrores.java`,
    `<base>/config/AppConfig.java`, `<base>/config/CorsConfig.java`, `<base>/config/RabbitConfig.java`.
    Elimina `src/test/java/com/subacol/msagenda/MsAgendaApplicationTests.java`.
  - Hecho cuando: la imagen se construye y, con el sistema arriba, `GET /actuator/health` en el
    puerto 8082 responde `UP`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-agenda services\ms-agenda; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.2, REQ-1.3, REQ-1.6, REQ-2.1, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_
  - Nota (2026-11-12, D-22): `ms-agenda` pasa a Maven. `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` y el
    Dockerfile de Maven (con `EXPOSE 8082`) reemplazan a `build.gradle` y al Dockerfile de Gradle.

- [ ] 3. ms-auditoria: base del proyecto
  - Archivos: `services/ms-auditoria/pom.xml`, `src/main/resources/application.properties`,
    `Dockerfile`, `.dockerignore`, `db/init.sql` (encabezado), `<base>/domain/ErrorDeNegocio.java`,
    `<base>/adapter/in/web/ManejadorErrores.java`, `<base>/config/AppConfig.java`,
    `<base>/config/CorsConfig.java`, `<base>/config/RabbitConfig.java`. Elimina
    `src/test/java/com/subacol/msauditoria/MsAuditoriaApplicationTests.java`.
  - Hecho cuando: la imagen se construye y, con el sistema arriba, `GET /actuator/health` en el
    puerto 8083 responde `UP`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-auditoria services\ms-auditoria; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.2, REQ-1.3, REQ-1.6, REQ-2.1, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_

- [ ] 4. ms-notificaciones: base del proyecto
  - Archivos: `services/ms-notificaciones/pom.xml` (dependencias y Java 21),
    `src/main/resources/application.properties`, `Dockerfile`, `.dockerignore`, `db/init.sql`
    (encabezado), `<base>/domain/ErrorDeNegocio.java`, `<base>/config/AppConfig.java`,
    `<base>/config/RabbitConfig.java`. Elimina
    `src/test/java/com/subacol/msnotificaciones/MsNotificacionesApplicationTests.java`.
  - Hecho cuando: la imagen se construye y, con el sistema arriba, `GET /actuator/health` en el
    puerto 8084 responde `UP`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/ms-notificaciones services\ms-notificaciones; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.2, REQ-1.3, REQ-1.6, REQ-2.1_

- [ ] 5. garageflow-web: base del frontend
  - Archivos: `garageflow-web/Dockerfile`, `.dockerignore`, `public/config.json`,
    `src/app/app.routes.server.ts`, `src/app/app.config.ts`, `src/app/app.component.ts`,
    `app.component.html`, `app.component.css`, `app.component.spec.ts`,
    `src/app/core/config/configuracion.service.ts`, `src/app/core/usuario/usuario.service.ts`,
    `src/app/core/usuario/usuario.interceptor.ts`, `usuario.interceptor.spec.ts`,
    `src/app/core/errores/mensaje-error.ts`, `mensaje-error.spec.ts`. No cambian `src/server.ts`,
    `src/main.server.ts` ni `src/app/app.config.server.ts`: `ng build` los compila tal como los
    generó el CLI.
  - Hecho cuando: la imagen se construye y `http://localhost:4200` muestra la barra superior con
    "Órdenes", "Auditoría" y el selector de usuario.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/garageflow-web garageflow-web; Write-Host "EXIT_CODE_$LASTEXITCODE"`
    (opcional, con Chrome: `Set-Location 'c:\repos_cobis\Prueba\garageflow-web'; npx ng test --watch=false --browsers=ChromeHeadless; Write-Host "EXIT_CODE_$LASTEXITCODE"`)
  - _Requisitos: REQ-1.5, REQ-4.2, REQ-5.1, REQ-5.2_

- [ ] 6. infraestructura: docker-compose y pruebas de salud
  - Archivos: `docker-compose.yml`, `.env.example`, `.gitignore` (ignora `.env`), `README.md`
    (prerrequisitos, cómo levantar, URLs, scripts, cómo reiniciar las bases),
    `scripts/prueba-salud.ps1`.
  - Hecho cuando: `docker compose config` no da errores, `docker compose up --build -d` deja los
    10 contenedores en ejecución y `scripts/prueba-salud.ps1` termina con `EXIT_CODE_0`.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker compose up --build -d; docker compose ps; powershell -ExecutionPolicy Bypass -File .\scripts\prueba-salud.ps1; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.4, REQ-1.5, REQ-2.1, REQ-4.1_

- [ ] 8. infraestructura: datos de ejemplo al levantar
  - Archivos: `seed/seed.mjs`, `docker-compose.yml` (servicio `datos-ejemplo`)
  - Hecho cuando: con bases vacías, `docker compose up -d` deja 6 o más órdenes y 3 o más citas,
    y una segunda corrida de `datos-ejemplo` no crea nada.
  - Validación: `docker compose logs datos-ejemplo`
  - _Requisitos: REQ-6.1, REQ-6.2, REQ-6.3_

- [ ] 9. infraestructura: carpeta `garageflow-services` y README corto
  - Archivos: `docker-compose.yml` (rutas), `README.md`
  - Hecho cuando: `docker compose up -d --build` construye desde `garageflow-services/` y el
    README explica en pocas líneas cómo levantar, los datos de ejemplo, los usuarios y cómo
    funciona.
  - Validación: `docker compose up -d --build`
  - _Requisitos: D-27, REQ-1.1_

- [ ] 7. Verificación del spec
  - Hecho cuando: cada criterio tiene una prueba con evidencia del Developer o una revisión
    manual registrada, y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 6 |
| REQ-1.2 | 1, 2, 3, 4 |
| REQ-1.3 | 1, 2, 3, 4 |
| REQ-1.4 | 6 |
| REQ-1.5 | 5, 6 |
| REQ-1.6 | 1, 2, 3, 4 |
| REQ-2.1 | 1, 2, 3, 4, 6 |
| REQ-3.1 | 1, 2, 3 |
| REQ-3.2 | 1, 2, 3 |
| REQ-3.3 | 1, 2, 3 |
| REQ-4.1 | 1, 2, 3, 6 |
| REQ-4.2 | 5 |
| REQ-5.1 | 5 |
| REQ-5.2 | 5 |
| REQ-6.1 | 8 |
| REQ-6.2 | 8 |
| REQ-6.3 | 8 |

## Cambios

- 2026-11-12: tarea 2, `ms-agenda` se construye con Maven en lugar de Gradle (D-22). La red de la
  empresa bloquea la descarga de Gradle dentro de Docker. Lo pidió el Developer.
- 2026-11-12: se agregan las tareas 8 (datos de ejemplo, D-26) y 9 (carpeta
  `garageflow-services` y README, D-27), pedidas por el Developer. La 7 sigue siendo la
  verificación final.
