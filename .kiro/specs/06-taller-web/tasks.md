# Tareas: 06-taller-web

Estado: APROBADO (2026-11-12, Developer)

- Rutas relativas a `c:\repos_cobis\Prueba\garageflow-web`. `<app>` es `src/app`.
- Las pruebas del frontend llevan en `it(...)` el spec y el criterio (convención de `00-base`).
- Solo el coordinador marca `[x]`. Con D-20, las validaciones se corren juntas al final.

## Tareas

- [ ] 1. garageflow-web: acceso a datos
  - Archivos: `<app>/features/ordenes/data-access/ordenes.models.ts`, `ordenes.api.ts`;
    `<app>/features/agenda/data-access/agenda.models.ts`, `agenda.api.ts`;
    `<app>/features/auditoria/data-access/auditoria.models.ts`, `auditoria.api.ts`.
  - Hecho cuando: los 3 servicios cubren las llamadas de "Contratos que usa", arman las URLs con
    `ConfiguracionService` y no hay `http://` en `<app>`.
  - Validación: revisión estática.
  - _Requisitos: REQ-1.2, REQ-2.3, REQ-3.2, REQ-3.3, REQ-3.4, REQ-4.1, REQ-4.2_

- [ ] 2. garageflow-web: rutas y página de órdenes
  - Archivos: `<app>/app.routes.ts`, `<app>/features/ordenes/pages/ordenes-page.component.ts`,
    `src/styles.css`.
  - Hecho cuando: `/ordenes` lista las órdenes y registra una nueva, con los errores de la API a
    la vista.
  - Validación: `Set-Location 'c:\repos_cobis\Prueba'; docker build -t garageflow/garageflow-web garageflow-web; Write-Host "EXIT_CODE_$LASTEXITCODE"`
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.3, REQ-1.4, REQ-5.1, REQ-5.2, REQ-5.3_

- [ ] 3. garageflow-web: detalle de la orden y sus acciones
  - Archivos: `<app>/features/ordenes/pages/orden-detalle-page.component.ts`,
    `orden-detalle-page.component.spec.ts`.
  - Hecho cuando: el detalle muestra la orden y solo las acciones permitidas, y reabrir y avanzar
    funcionan contra la API.
  - Validación: la de la tarea 2 (opcional, con Chrome: `Set-Location 'c:\repos_cobis\Prueba\garageflow-web'; npx ng test --watch=false --browsers=ChromeHeadless; Write-Host "EXIT_CODE_$LASTEXITCODE"`)
  - _Requisitos: REQ-2.1, REQ-2.2, REQ-2.3, REQ-2.4, REQ-2.5, REQ-2.6, REQ-5.1, REQ-5.2_

- [ ] 4. garageflow-web: citas de la orden
  - Archivos: `<app>/features/agenda/components/citas-orden.component.ts`.
  - Hecho cuando: desde el detalle se ven, se agendan y se cancelan citas, con los errores de la
    API a la vista.
  - Validación: la de la tarea 2.
  - _Requisitos: REQ-3.1, REQ-3.2, REQ-3.3, REQ-3.4, REQ-3.5, REQ-5.1, REQ-5.2_

- [ ] 5. garageflow-web: historial y página de auditoría
  - Archivos: `<app>/features/auditoria/components/historial-tabla.component.ts`,
    `<app>/features/auditoria/pages/auditoria-page.component.ts`.
  - Hecho cuando: el detalle y `/auditoria` muestran el historial.
  - Validación: la de la tarea 2.
  - _Requisitos: REQ-4.1, REQ-4.2_

- [ ] 6. Verificación del spec
  - Hecho cuando: el Developer recorre en `http://localhost:4200` las revisiones manuales de la
    estrategia de pruebas y registra el resultado de cada una, la prueba unitaria pasa o queda
    registrada como no ejecutada por falta de Chrome, y la revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_
  - Nota (2026-11-12): se hace después de las tareas 7 a 10.

- [ ] 7. garageflow-web: Tailwind, base visual y limpieza del SSR
  - Archivos: `package.json`, `.postcssrc.json`, `angular.json`, `tsconfig.app.json`,
    `src/styles.css`, `<app>/app.component.ts`, `<app>/core/config/configuracion.service.ts`,
    `<app>/shared/ui/etiquetas.ts`. El coordinador borró `src/server.ts`, `src/main.server.ts`,
    `<app>/app.config.server.ts` y `<app>/app.routes.server.ts` (D-24) y actualiza
    `package-lock.json`.
  - Hecho cuando: `ng build` compila con Tailwind y `http://localhost:4200` muestra la barra
    superior nueva.
  - Validación: la de la tarea 2.
  - _Requisitos: REQ-6.1, REQ-6.3, REQ-6.6, REQ-5.3_

- [ ] 8. garageflow-web: pantallas de órdenes y flujo de la orden
  - Archivos: `<app>/features/ordenes/pages/ordenes-page.component.ts`,
    `orden-detalle-page.component.ts`, `orden-detalle-page.component.spec.ts`,
    `<app>/features/ordenes/components/flujo-orden.component.ts`.
  - Hecho cuando: `/ordenes` muestra los contadores por etapa, el formulario y la tabla con el
    diseño nuevo, y el detalle muestra el flujo con las acciones debajo.
  - Validación: la de la tarea 2.
  - _Requisitos: REQ-6.2, REQ-6.3, REQ-6.4, REQ-6.6, REQ-1.1, REQ-1.3, REQ-2.1, REQ-2.2, REQ-5.1, REQ-5.2_

- [ ] 9. garageflow-web: citas, historial y auditoría
  - Archivos: `<app>/features/agenda/components/citas-orden.component.ts`,
    `<app>/features/auditoria/components/historial-tabla.component.ts`,
    `historial-linea-tiempo.component.ts`, `<app>/features/auditoria/pages/auditoria-page.component.ts`.
  - Hecho cuando: el detalle muestra las citas y la línea de tiempo, y `/auditoria` muestra la
    tabla, con el diseño nuevo.
  - Validación: la de la tarea 2.
  - _Requisitos: REQ-6.3, REQ-6.5, REQ-6.6, REQ-3.1, REQ-4.1, REQ-4.2, REQ-5.1, REQ-5.2_

- [ ] 10. infraestructura: quitar `NG_ALLOWED_HOSTS`
  - Archivos: `docker-compose.yml`, `README.md`.
  - Hecho cuando: `garageflow-web` ya no tiene variables de entorno en `docker-compose.yml`,
    `http://localhost:4200` responde 200 y el README menciona Tailwind.
  - Validación: `powershell -ExecutionPolicy Bypass -File .\scripts\prueba-salud.ps1`
  - _Requisitos: D-24_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 2 |
| REQ-1.2 | 1, 2 |
| REQ-1.3 | 2 |
| REQ-1.4 | 2 |
| REQ-2.1 | 3 |
| REQ-2.2 | 3 |
| REQ-2.3 | 1, 3 |
| REQ-2.4 | 3 |
| REQ-2.5 | 3 |
| REQ-2.6 | 3 |
| REQ-3.1 | 4 |
| REQ-3.2 | 1, 4 |
| REQ-3.3 | 1, 4 |
| REQ-3.4 | 1, 4 |
| REQ-3.5 | 4 |
| REQ-4.1 | 1, 5 |
| REQ-4.2 | 1, 5 |
| REQ-5.1 | 2, 3, 4 |
| REQ-5.2 | 2, 3, 4 |
| REQ-5.3 | 2, 7 |
| REQ-6.1 | 7 |
| REQ-6.2 | 8 |
| REQ-6.3 | 7, 8, 9 |
| REQ-6.4 | 8 |
| REQ-6.5 | 9 |
| REQ-6.6 | 7, 8, 9 |

## Cambios

- 2026-11-12: se agregan las tareas 7 a 10 (REQ-6, D-23, D-24). Lo pidió el Developer.
