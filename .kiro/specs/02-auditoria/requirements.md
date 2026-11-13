# Requisitos: 02-auditoria

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: guardar cada cambio de estado de las órdenes con quién y cuándo, en un registro que
  nadie pueda alterar, y consultarlo.
- Decisiones que aplica: D-04, D-07, D-10, D-12, D-21.
- Fuera de alcance de este spec: auditar las citas, y detectar cambios hechos por un
  administrador de MySQL que borre los triggers ("Fuera de alcance" de `DECISIONES.md`).

## Glosario

- **Historial**: la lista de cambios de estado de las órdenes que guarda `ms-auditoria`.
- **Cambio de estado**: un registro del historial: orden, estado anterior, estado nuevo, motivo,
  usuario, origen y fecha.

## Requisitos

### REQ-1 — Registrar cada cambio de estado

**Historia:** Como auditor, quiero que cada cambio de estado de una orden quede registrado con
quién lo hizo y cuándo, para responder cualquier revisión.

**Origen:** enunciado ("auditoría pide saber quién cambió cada estado y cuándo")

**Criterios de aceptación**

1. CUANDO llega `EstadoOrdenCambiado`, el sistema DEBE guardar en el historial `eventoId`,
   `ordenId`, `estadoAnterior`, `estadoNuevo`, `motivo`, `usuario`, `origen` y `ocurridoEn`.
2. SI llega dos veces un evento con el mismo `eventoId`, ENTONCES el sistema DEBE guardarlo una
   sola vez.
3. SI el evento no trae `eventoId`, `ordenId`, `estadoNuevo`, `usuario`, `origen` u `ocurridoEn`,
   ENTONCES el sistema DEBE rechazarlo sin guardarlo, y después de 3 intentos el mensaje DEBE
   quedar en la cola `auditoria.orden-estado-cambiado.dlq`.

### REQ-2 — Nadie puede alterar el registro

**Historia:** Como auditor, quiero que nadie pueda cambiar ni borrar el historial, para poder
confiar en él.

**Origen:** enunciado ("sin que nadie pueda alterar ese registro"), decisión D-10

**Criterios de aceptación**

1. SI alguien ejecuta `UPDATE` o `DELETE` sobre el historial, con cualquier usuario de MySQL,
   ENTONCES la base de datos DEBE rechazar la operación con un error y no cambiar ningún registro.
2. El usuario de MySQL con el que se conecta `ms-auditoria` DEBE tener solo los permisos `SELECT`
   e `INSERT` sobre el historial.

### REQ-3 — Consultar el historial

**Historia:** Como auditor, quiero ver el historial de una orden y los últimos cambios del taller,
para saber quién cambió cada estado y cuándo.

**Origen:** enunciado

**Criterios de aceptación**

1. CUANDO se consulta el historial de una orden, el sistema DEBE devolver sus cambios del más
   antiguo al más reciente.
2. CUANDO se consulta el historial sin indicar una orden, el sistema DEBE devolver los últimos 200
   cambios de todas las órdenes, del más reciente al más antiguo.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| Cualquier usuario puede consultar el historial (no hay autenticación, D-14) | REQ-3.1, REQ-3.2 | Habría que filtrar por rol cuando exista autenticación |

## Cambios

- (sin cambios)
