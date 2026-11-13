# Tareas: 07-roles

Estado: APROBADO (2026-11-12, Developer)

- Las pruebas citan el spec y el criterio en `@DisplayName` o en `it(...)`, por ejemplo
  `07 REQ-3.2 el AUDITOR no puede modificar`.
- Solo el coordinador marca `[x]`.

## Tareas

- [ ] 1. ms-ordenes: rol en el borde
  - Archivos: `<ordenes>/domain/ErrorDeNegocio.java`, `<ordenes>/adapter/in/web/Rol.java`,
    `RolInterceptor.java`, `ManejadorErrores.java`, `OrdenesController.java`, `OrdenResponse.java`;
    `<ordenes>/config/RolConfig.java`; pruebas `RolInterceptorTest.java` y `OrdenResponseTest.java`.
  - Hecho cuando: la imagen se construye con las pruebas nuevas en verde.
  - Validación: `docker build` de `services\ms-ordenes`.
  - _Requisitos: REQ-1.3, REQ-2.1, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_

- [ ] 2. ms-agenda: rol en el borde y reenvío a ms-ordenes
  - Archivos: los mismos que en la tarea 1 en `<agenda>` (con `AgendaController.java` y
    `CitaResponse.java`), y `<agenda>/adapter/out/http/OrdenesRestClient.java`; pruebas
    `RolInterceptorTest.java` y `CitaResponseTest.java`.
  - Hecho cuando: la imagen se construye con las pruebas nuevas en verde.
  - Validación: `docker build` de `services\ms-agenda`.
  - _Requisitos: REQ-1.3, REQ-2.1, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_

- [ ] 3. ms-auditoria: rol en el borde y regla del historial general
  - Archivos: `<auditoria>/domain/ErrorDeNegocio.java`, `<auditoria>/adapter/in/web/Rol.java`,
    `RolInterceptor.java`, `ManejadorErrores.java`; `<auditoria>/config/RolConfig.java`; prueba
    `RolInterceptorTest.java`.
  - Hecho cuando: la imagen se construye con las pruebas nuevas en verde.
  - Validación: `docker build` de `services\ms-auditoria`.
  - _Requisitos: REQ-1.3, REQ-2.2, REQ-2.3, REQ-3.1, REQ-4.1_

- [ ] 4. garageflow-web: usuarios con rol
  - Archivos: `<app>/core/usuario/usuario.service.ts`, `usuario.interceptor.ts`,
    `usuario.interceptor.spec.ts`, `rol.guard.ts`; `<app>/app.routes.ts`, `app.component.ts`,
    `app.component.spec.ts`; `<app>/features/ordenes/pages/ordenes-page.component.ts`;
    `<app>/features/agenda/components/citas-orden.component.ts`.
  - Hecho cuando: `ng build` compila y cada usuario ve lo que le toca.
  - Validación: `docker build` de `garageflow-web`.
  - _Requisitos: REQ-1.1, REQ-1.2, REQ-1.4, REQ-2.4, REQ-3.4, REQ-4.1_

- [ ] 5. infraestructura: scripts con `X-Rol` y prueba de roles
  - Archivos: `scripts/prueba-salud.ps1`, `prueba-ordenes.ps1`, `prueba-auditoria.ps1`,
    `prueba-cupo.ps1`, `prueba-cierre.ps1`, `scripts/prueba-roles.ps1` (nuevo), `README.md`.
  - Hecho cuando: los 6 scripts terminan con `RESULTADO: TODOS PASS`.
  - Validación: los 6 scripts contra el sistema levantado.
  - _Requisitos: REQ-1.3, REQ-2.1, REQ-2.3, REQ-3.1, REQ-3.2, REQ-3.3, REQ-4.1_

- [ ] 6. Verificación del spec
  - Hecho cuando: cada criterio tiene prueba con evidencia o revisión manual registrada, y la
    revisión de trazabilidad da PASS.
  - _Requisitos: todos los de este spec_

## Cobertura

| Criterio | Tareas |
|---|---|
| REQ-1.1 | 4 |
| REQ-1.2 | 4 |
| REQ-1.3 | 1, 2, 3, 5 |
| REQ-1.4 | 4 |
| REQ-2.1 | 1, 2, 5 |
| REQ-2.2 | 3 |
| REQ-2.3 | 3, 5 |
| REQ-2.4 | 4 |
| REQ-3.1 | 1, 2, 3, 5 |
| REQ-3.2 | 1, 2, 5 |
| REQ-3.3 | 1, 2, 5 |
| REQ-3.4 | 4 |
| REQ-4.1 | 1, 2, 3, 4, 5 |

## Cambios

- (sin cambios)
