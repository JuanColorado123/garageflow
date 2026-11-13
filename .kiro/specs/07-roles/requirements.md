# Requisitos: 07-roles

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: tres roles de demostración (asesor, auditor y admin) que limitan qué puede consultar
  y qué puede cambiar cada usuario.
- Decisiones que aplica: D-14, D-25.
- Fuera de alcance de este spec: inicio de sesión y contraseñas (D-14), administración de usuarios
  y roles desde la pantalla, y permisos por orden o por asesor.

## Glosario

- **Historial general**: `GET /api/auditoria/historial` sin `ordenId`, los últimos cambios de todas
  las órdenes (pantalla Auditoría).
- **Historial de una orden**: el mismo endpoint con `ordenId`, que usa el detalle de la orden.

## Requisitos

### REQ-1 — Usuarios, roles y cabecera `X-Rol`

**Historia:** Como usuario de la demostración, quiero elegir un usuario con su rol para probar qué
puede hacer cada uno.

**Origen:** decisión D-25 (pedido del Developer, 2026-11-12)

**Criterios de aceptación**

1. El frontend DEBE ofrecer en el selector 4 usuarios: `asesor.ana` y `asesor.luis` con rol
   `ASESOR`, `auditor.marta` con rol `AUDITOR` y `admin.sofia` con rol `ADMIN`, y mostrar el rol
   del usuario elegido.
2. CUANDO el frontend llama a un microservicio, DEBE enviar la cabecera `X-Rol` con el rol del
   usuario elegido.
3. SI una petición a `/api/**` de `ms-ordenes`, `ms-agenda` o `ms-auditoria` no trae `X-Rol`, o
   trae un valor distinto de `ASESOR`, `AUDITOR` o `ADMIN`, ENTONCES el microservicio DEBE
   responder 403 `ROL_NO_PERMITIDO` sin cambiar nada.
4. CUANDO el usuario elige otro usuario, el frontend DEBE volver a cargar la pantalla con el
   usuario nuevo y conservarlo si se recarga el navegador.

### REQ-2 — Asesor

**Historia:** Como asesor, quiero manejar las órdenes y sus citas.

**Origen:** pedido del Developer ("el asesor puede solo ver órdenes")

**Criterios de aceptación**

1. MIENTRAS el rol es `ASESOR`, el sistema DEBE permitir consultar, registrar, avanzar y reabrir
   órdenes, y consultar, agendar y cancelar citas.
2. CUANDO un `ASESOR` pide el historial de una orden, `ms-auditoria` DEBE responderlo.
3. SI un `ASESOR` pide el historial general, ENTONCES `ms-auditoria` DEBE responder 403
   `ROL_NO_PERMITIDO`.
4. El frontend NO DEBE mostrar el enlace "Auditoría" a un `ASESOR`, y CUANDO un `ASESOR` abre
   `/auditoria`, DEBE llevarlo a `/ordenes`.

### REQ-3 — Auditor: solo consulta

**Historia:** Como auditor, quiero revisar las órdenes y su historial sin poder cambiar nada.

**Origen:** pedido del Developer ("el auditor solo puede ver")

**Criterios de aceptación**

1. MIENTRAS el rol es `AUDITOR`, el sistema DEBE permitir consultar órdenes, citas, asesores,
   disponibilidad, el historial de una orden y el historial general.
2. SI un `AUDITOR` intenta registrar, avanzar o reabrir una orden, o agendar o cancelar una cita,
   ENTONCES el microservicio DEBE responder 403 `ROL_NO_PERMITIDO` sin cambiar nada.
3. CUANDO responden a un `AUDITOR`, `ms-ordenes` y `ms-agenda` DEBEN devolver `accionesPermitidas`
   vacío en cada orden y cada cita.
4. El frontend NO DEBE mostrar a un `AUDITOR` los formularios "Nueva orden" y "Agendar cita".

### REQ-4 — Admin

**Historia:** Como administrador, quiero manejar las órdenes y también revisar la auditoría.

**Origen:** pedido del Developer ("el admin puede ver ambos")

**Criterios de aceptación**

1. MIENTRAS el rol es `ADMIN`, el sistema DEBE permitir todo lo que permite al `ASESOR` y además
   el historial general y la pantalla Auditoría.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| El auditor también ve las órdenes, sin cambiarlas | REQ-3.1, REQ-3.3 | Las consultas de `ms-ordenes` y `ms-agenda` también le responderían 403, y el frontend le ocultaría "Órdenes" |
| El admin puede hacer todo lo que hace el asesor | REQ-4.1 | El admin solo consultaría, como el auditor |
| El usuario admin de la demostración se llama `admin.sofia` | REQ-1.1 | Solo cambia la lista del frontend |

## Cambios

- (sin cambios)
