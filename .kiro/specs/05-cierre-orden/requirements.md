# Requisitos: 05-cierre-orden

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: cuando una orden se cierra, cancelar solas sus citas pendientes sin avisarle al
  cliente.
- Decisiones que aplica: D-07, D-08.
- Fuera de alcance de este spec: restaurar las citas si la orden se reabre, y avisar al cliente
  que su orden se cerró.

## Glosario

- **Cancelación por cierre**: cancelación de una cita con motivo `CIERRE_ORDEN`, causada por el
  evento `OrdenCerrada`.

## Requisitos

### REQ-1 — Cancelar las citas pendientes al cerrar la orden

**Historia:** Como taller, quiero que al cerrar una orden se cancelen solas sus citas pendientes,
para no dejar citas de vehículos que ya se entregaron.

**Origen:** enunciado ("al cerrar una orden, sus citas pendientes se cancelan solas")

**Criterios de aceptación**

1. CUANDO llega `OrdenCerrada`, `ms-agenda` DEBE dejar `CANCELADA`, con motivo `CIERRE_ORDEN`,
   cada cita `PENDIENTE` de esa orden.
2. CUANDO cancela una cita por cierre, `ms-agenda` DEBE publicar `CitaCancelada` versión 1 con
   motivo `CIERRE_ORDEN`.
3. SI llega dos veces un `OrdenCerrada` con el mismo `eventoId`, ENTONCES `ms-agenda` DEBE
   procesarlo una sola vez.
4. El cierre DEBE dejar sin cambios las citas de la orden que ya estaban `CANCELADA`, con su
   motivo original.

### REQ-2 — No avisar al cliente cuando la cancelación es por cierre

**Historia:** Como cliente, no quiero recibir un aviso de cancelación cuando ya recogí mi
vehículo.

**Origen:** enunciado ("en ese caso no se le avisa al cliente, porque ya recogió su vehículo"),
decisión D-08

**Criterios de aceptación**

1. SI llega `CitaCancelada` con motivo `CIERRE_ORDEN`, ENTONCES `ms-notificaciones` DEBE omitir el
   WhatsApp: no escribe ninguna línea `WHATSAPP_SIMULADO` para esa cancelación.
2. CUANDO omite un aviso, `ms-notificaciones` DEBE guardarlo con estado `OMITIDO` y escribir en el
   log una línea `WHATSAPP_OMITIDO` con la cita y el motivo.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| Si la orden se reabre, las citas canceladas por cierre siguen canceladas; el asesor agenda otras | REQ-1.1 | Habría que restaurar citas al recibir la reapertura |

## Cambios

- (sin cambios)
