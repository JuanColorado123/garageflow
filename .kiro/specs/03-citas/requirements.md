# Requisitos: 03-citas

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: consultar la disponibilidad de los asesores, agendar citas para traer o recoger el
  vehículo sin superar el cupo por franja, cancelarlas y avisar esos cambios.
- Decisiones que aplica: D-03, D-05, D-06, D-07, D-19, D-21.
- Fuera de alcance de este spec: la cancelación automática al cerrar la orden (spec
  `05-cierre-orden`), los avisos al cliente (spec `04-avisos-whatsapp`), reprogramar citas y
  administrar asesores ("Fuera de alcance" de `DECISIONES.md`).

## Glosario

- **Franja**: bloque de 1 hora que empieza en punto.
- **Cupo**: cantidad máxima de citas `PENDIENTE` que un asesor atiende en una franja.
- **Cita**: visita del cliente para traer (`TRAER`) o recoger (`RECOGER`) su vehículo.

## Requisitos

### REQ-1 — Asesores y disponibilidad

**Historia:** Como asesor, quiero ver qué franjas tienen cupo, para ofrecerle al cliente un
horario que se pueda cumplir.

**Origen:** enunciado ("cada asesor atiende un número limitado de citas por franja horaria"); el
horario y los asesores iniciales son supuestos (D-03)

**Criterios de aceptación**

1. CUANDO se consultan los asesores, el sistema DEBE devolver cada uno con su `id`, su `nombre` y
   su `cupoPorFranja`.
2. CUANDO se consulta la disponibilidad de una fecha, el sistema DEBE devolver, para cada asesor y
   cada franja de esa fecha que todavía no empezó, el `cupo`, las citas `ocupadas` (en estado
   `PENDIENTE`) y las `libres`.
3. SI la fecha es domingo o ya pasó, ENTONCES el sistema DEBE devolver una lista vacía.

### REQ-2 — Agendar una cita

**Historia:** Como asesor, quiero agendar una cita para que el cliente traiga o recoja su
vehículo, sin pasar el cupo del asesor.

**Origen:** enunciado

**Criterios de aceptación**

1. CUANDO se agenda una cita con `ordenId`, `asesorId`, `franjaInicio` y `tipo` (`TRAER` o
   `RECOGER`) y el asesor tiene cupo en esa franja, el sistema DEBE crear la cita en estado
   `PENDIENTE`, con el nombre y el teléfono del cliente de la orden, y responder 201 con la cita.
2. SI el asesor ya tiene en esa franja tantas citas `PENDIENTE` como su cupo, ENTONCES el sistema
   DEBE responder 409 `SIN_CUPO` sin crear la cita.
3. MIENTRAS lleguen varias reservas al mismo tiempo para el mismo asesor y la misma franja, el
   sistema DEBE crear como máximo tantas citas como el cupo del asesor.
4. SI la franja no empieza en punto, no es de lunes a sábado entre las 08:00 y las 16:00, o ya
   empezó, ENTONCES el sistema DEBE responder 400 `FRANJA_INVALIDA`.
5. SI la orden no existe, ENTONCES el sistema DEBE responder 404 `ORDEN_NO_ENCONTRADA`.
6. SI la orden está `CERRADA`, ENTONCES el sistema DEBE responder 409 `ORDEN_CERRADA`.
7. SI el asesor no existe, ENTONCES el sistema DEBE responder 404 `ASESOR_NO_ENCONTRADO`.
8. SI `ms-ordenes` no responde en 2 segundos o responde con error, ENTONCES el sistema DEBE
   responder 503 `ORDENES_NO_DISPONIBLE` sin crear la cita.
9. SI falta `ordenId`, `asesorId`, `franjaInicio` o `tipo`, o el `tipo` no es `TRAER` ni
   `RECOGER`, ENTONCES el sistema DEBE responder 400 `DATOS_INVALIDOS`.

### REQ-3 — Cancelar una cita

**Historia:** Como asesor, quiero cancelar una cita, para liberar el cupo cuando el cliente no
puede venir.

**Origen:** enunciado ("cuando se le agenda o cancela una cita")

**Criterios de aceptación**

1. CUANDO se cancela una cita `PENDIENTE`, el sistema DEBE dejarla `CANCELADA` con motivo
   `MANUAL`, liberar su lugar en el cupo de la franja y responder 200 con la cita.
2. SI la cita ya está `CANCELADA`, ENTONCES el sistema DEBE responder 409 `CITA_NO_PENDIENTE`.
3. SI la cita no existe, ENTONCES el sistema DEBE responder 404 `CITA_NO_ENCONTRADA`.

### REQ-4 — Citas de una orden

**Historia:** Como asesor, quiero ver las citas de una orden, para saber cuándo viene el cliente.

**Origen:** enunciado

**Criterios de aceptación**

1. CUANDO se consultan las citas de una orden, el sistema DEBE devolverlas todas, con su estado y
   su motivo de cancelación, ordenadas por franja.
2. El sistema DEBE incluir en cada cita sus `accionesPermitidas`: `CANCELAR` si está `PENDIENTE`,
   y ninguna si está `CANCELADA`. (Origen: decisión D-13, la interfaz muestra solo las acciones
   que permite la API)

### REQ-5 — Avisar los cambios de las citas

**Historia:** Como notificaciones, quiero enterarme de cada cita agendada o cancelada, para avisar
al cliente.

**Origen:** decisiones D-05 y D-07

**Criterios de aceptación**

1. CUANDO se crea una cita, el sistema DEBE publicar `CitaAgendada` versión 1 con `citaId`,
   `ordenId`, `asesorId`, `franjaInicio`, `tipoCita`, `clienteNombre`, `clienteTelefono` y
   `ocurridoEn`.
2. CUANDO se cancela una cita, el sistema DEBE publicar `CitaCancelada` versión 1 con los mismos
   datos de la cita, el `motivo` de la cancelación y `ocurridoEn`.
3. El sistema DEBE guardar la cita y su evento en la misma transacción: o se guardan los dos, o
   ninguno.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| El taller atiende de lunes a sábado; las franjas empiezan entre las 08:00 y las 16:00 | REQ-1.2, REQ-1.3, REQ-2.4 | La regla de `Franja` |
| Hay 2 asesores iniciales, `1 Ana Gómez` y `2 Luis Pérez`, con cupo 2 por franja | REQ-1.1, REQ-2.2 | Los datos de `db/init.sql` |
| Se puede agendar en cualquier estado de la orden menos `CERRADA` | REQ-2.6 | La validación del caso de uso |

## Cambios

- (sin cambios)
