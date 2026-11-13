# Diseño: 07-roles

Estado: APROBADO (2026-11-12, Developer)

## Resumen

El rol viaja en la cabecera `X-Rol`, igual que el usuario en `X-Usuario` (D-14). Cada
microservicio con API lo comprueba en el borde, con un `HandlerInterceptor` de Spring MVC, antes
de llegar al controlador. Así trabajaría Spring Security con un login real: cuando exista, el rol
saldrá del token y el resto no cambia. El frontend solo adapta lo que muestra; quien decide es la
API.

Cubre: REQ-1, REQ-2, REQ-3, REQ-4.

## Decisiones aplicadas

- D-14: usuario simulado, sin autenticación.
- D-25 (nueva): roles simulados con `X-Rol`, comprobados en cada microservicio.

## Proyectos afectados

| Proyecto | Qué cambia | Datos propios que toca |
|---|---|---|
| `ms-ordenes` | Interceptor de rol; `accionesPermitidas` vacío para `AUDITOR` | — |
| `ms-agenda` | Interceptor de rol; `accionesPermitidas` vacío para `AUDITOR`; reenvía `X-Rol` y `X-Usuario` al consultar `ms-ordenes` | — |
| `ms-auditoria` | Interceptor de rol con la regla del historial general | — |
| `garageflow-web` | Usuarios con rol, cabecera `X-Rol`, menú y formularios según el rol | — |
| `ms-notificaciones` | Nada: no tiene API | — |

## Ubicación

En cada microservicio con API (`<base>` es su paquete base):

| Clase | Capa | Responsabilidad |
|---|---|---|
| `<base>/adapter/in/web/Rol.java` | adapter.in.web | `enum Rol { ASESOR, AUDITOR, ADMIN }`, `desde(String)` y `puedeModificar()` (`ASESOR` y `ADMIN`); `ms-auditoria` tiene `puedeVerHistorialGeneral()` (`AUDITOR` y `ADMIN`) |
| `<base>/adapter/in/web/RolInterceptor.java` | adapter.in.web | Lee `X-Rol`, deja pasar el preflight de CORS, lanza `ErrorDeNegocio(PROHIBIDO, "ROL_NO_PERMITIDO")` si el rol falta, no existe o no alcanza, y guarda el rol en el atributo de petición `rol` |
| `<base>/config/RolConfig.java` | config | Registra el interceptor para `/api/**` |
| `<base>/domain/ErrorDeNegocio.java` | domain | Nuevo `Tipo.PROHIBIDO` |
| `<base>/adapter/in/web/ManejadorErrores.java` | adapter.in.web | `PROHIBIDO` responde 403 |

Reglas de cada interceptor:

| Microservicio | Rutas | `ASESOR` | `AUDITOR` | `ADMIN` |
|---|---|---|---|---|
| `ms-ordenes` | `GET /api/ordenes/**` | sí | sí | sí |
| `ms-ordenes` | `POST /api/ordenes/**` | sí | 403 | sí |
| `ms-agenda` | `GET /api/agenda/**` | sí | sí | sí |
| `ms-agenda` | `POST /api/agenda/**` | sí | 403 | sí |
| `ms-auditoria` | `GET /api/auditoria/historial?ordenId=...` | sí | sí | sí |
| `ms-auditoria` | `GET /api/auditoria/historial` sin `ordenId` | 403 | sí | sí |

- Las excepciones de `preHandle` pasan por `ManejadorErrores`, así que el 403 sale como problem
  detail con `codigo`, igual que los demás errores.
- `OrdenesController` y `AgendaController` reciben el rol con `@RequestAttribute("rol")`; si
  `rol.puedeModificar()` es falso, las respuestas llevan `accionesPermitidas` vacío
  (`OrdenResponse.desde(orden, puedeModificar)`, `CitaResponse.desde(cita, puedeModificar)`).
- `ms-agenda` llama a `GET /api/ordenes/{id}` al agendar. `OrdenesRestClient` copia `X-Rol` y
  `X-Usuario` de la petición en curso (`RequestContextHolder`) a esa llamada.

En `garageflow-web/src/app`:

| Archivo | Responsabilidad |
|---|---|
| `core/usuario/usuario.service.ts` | Lista de 4 usuarios con su rol; `usuario()`, `rol()`, `puedeModificar()`, `veAuditoria()`, `inicio()`; guarda el usuario en `sessionStorage` y recarga la página al cambiarlo |
| `core/usuario/usuario.interceptor.ts` | Agrega `X-Rol` junto a `X-Usuario` |
| `core/usuario/rol.guard.ts` | `canMatch` de `/auditoria`: si `veAuditoria()` es falso, redirige a `/ordenes` |
| `app.routes.ts` | Usa el guard en `/auditoria` |
| `app.component.ts` | Muestra "Auditoría" solo si `veAuditoria()`; muestra el rol junto al selector |
| `features/ordenes/pages/ordenes-page.component.ts` | Oculta "Nueva orden" si `puedeModificar()` es falso; la tabla ocupa todo el ancho |
| `features/agenda/components/citas-orden.component.ts` | Oculta "Agendar cita" si `puedeModificar()` es falso |

## Contratos

- Cabecera nueva `X-Rol` en todas las llamadas a `/api/**`, con `ASESOR`, `AUDITOR` o `ADMIN`.
- Error nuevo: 403 `ROL_NO_PERMITIDO`.
- `accionesPermitidas` de órdenes y citas ahora depende del rol (vacío para `AUDITOR`).

## Errores y casos límite

| Caso | Respuesta | Criterio |
|---|---|---|
| Petición sin `X-Rol` o con `X-Rol: JEFE` | 403 `ROL_NO_PERMITIDO` | REQ-1.3 |
| Preflight de CORS (`OPTIONS`) | Pasa sin revisar el rol | REQ-1.3 |
| `POST` de un `AUDITOR` sin `X-Usuario` | 403 `ROL_NO_PERMITIDO`: el rol se revisa antes que el usuario | REQ-3.2 |
| `POST` de un `ASESOR` sin `X-Usuario` | 400 `USUARIO_REQUERIDO`, como hoy | REQ-2.1 |

## Estrategia de pruebas

| Criterio | Prueba | Tipo |
|---|---|---|
| REQ-1.1 | `app.component.spec.ts`: "07 REQ-1.1 el selector tiene los 4 usuarios"; a mano, el rol se ve junto al selector | unitaria (frontend, local), manual |
| REQ-1.2 | `usuario.interceptor.spec.ts`: "07 REQ-1.2 envía X-Rol con el rol elegido" | unitaria (frontend, local) |
| REQ-1.3 | `RolInterceptorTest` de los 3 microservicios: "07 REQ-1.3 sin X-Rol o con un rol desconocido responde ROL_NO_PERMITIDO" y "07 REQ-1.3 el preflight de CORS pasa"; `scripts/prueba-roles.ps1` | adaptador, manual |
| REQ-1.4 | Elegir otro usuario y recargar el navegador; sigue elegido | manual |
| REQ-2.1 | `RolInterceptorTest` (`ms-ordenes`, `ms-agenda`): "07 REQ-2.1 el ASESOR puede consultar y modificar"; `prueba-ordenes.ps1` y `prueba-cupo.ps1` con `X-Rol: ASESOR` | adaptador, manual |
| REQ-2.2 | `RolInterceptorTest` (`ms-auditoria`): "07 REQ-2.2 el ASESOR ve el historial de una orden" | adaptador |
| REQ-2.3 | `RolInterceptorTest` (`ms-auditoria`): "07 REQ-2.3 el ASESOR no ve el historial general"; `prueba-roles.ps1` | adaptador, manual |
| REQ-2.4 | Con `asesor.ana`, no aparece "Auditoría" y `/auditoria` lleva a `/ordenes` | manual |
| REQ-3.1 | `RolInterceptorTest` de los 3: "07 REQ-3.1 el AUDITOR puede consultar"; `prueba-roles.ps1` | adaptador, manual |
| REQ-3.2 | `RolInterceptorTest` (`ms-ordenes`, `ms-agenda`): "07 REQ-3.2 el AUDITOR no puede modificar"; `prueba-roles.ps1` | adaptador, manual |
| REQ-3.3 | `OrdenResponseTest` y `CitaResponseTest`: "07 REQ-3.3 sin permiso de modificar no hay acciones"; `prueba-roles.ps1` | adaptador, manual |
| REQ-3.4 | Con `auditor.marta`, no aparecen "Nueva orden" ni "Agendar cita" | manual |
| REQ-4.1 | `RolInterceptorTest` de los 3: "07 REQ-4.1 el ADMIN puede todo"; `prueba-roles.ps1` | adaptador, manual |

## Riesgos

- Cualquiera que llame a la API puede mandar el rol que quiera; es una demostración, igual que
  `X-Usuario` (D-14). Con un login real, el rol sale del token y se valida su firma.

## Cambios

- (sin cambios)
