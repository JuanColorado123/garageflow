# Requisitos: 04-avisos-whatsapp

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: avisar al cliente por WhatsApp, simulado con una línea de log, cuando se le agenda o
  se le cancela una cita.
- Decisiones que aplica: D-05, D-07, D-21.
- Fuera de alcance de este spec: no avisar cuando la cancelación es por cierre de la orden (spec
  `05-cierre-orden`), el WhatsApp real y el reenvío de avisos fallidos ("Fuera de alcance" de
  `DECISIONES.md`).

## Glosario

- **Aviso**: mensaje de WhatsApp para el cliente, con su estado (`ENVIADO` u `OMITIDO`).

## Requisitos

### REQ-1 — Avisar al cliente por WhatsApp

**Historia:** Como cliente, quiero recibir un WhatsApp cuando me agendan o me cancelan una cita,
para saber cuándo llevar o recoger mi vehículo.

**Origen:** enunciado ("el cliente recibe un WhatsApp cuando se le agenda o cancela una cita";
"el WhatsApp se puede simular con un log")

**Criterios de aceptación**

1. CUANDO llega `CitaAgendada`, el sistema DEBE escribir en el log una línea `WHATSAPP_SIMULADO`
   con la cita, el teléfono del cliente y el mensaje
   `Hola <nombre>, tu cita para <traer|recoger> tu vehículo quedó agendada el <dd/MM/aaaa> a las <HH:mm>.`
2. CUANDO llega `CitaCancelada` con motivo `MANUAL`, el sistema DEBE escribir en el log una línea
   `WHATSAPP_SIMULADO` con la cita, el teléfono del cliente y el mensaje
   `Hola <nombre>, tu cita del <dd/MM/aaaa> a las <HH:mm> fue cancelada.`
3. SI llega dos veces un evento con el mismo `eventoId`, ENTONCES el sistema DEBE avisar una sola
   vez.
4. CUANDO envía un aviso, el sistema DEBE guardarlo con su `eventoId`, la cita, la orden, el
   teléfono, el mensaje y el estado `ENVIADO`.
5. SI el evento no se puede leer o le falta `citaId`, `ordenId`, `franjaInicio`, `clienteNombre`
   o `clienteTelefono`, ENTONCES el sistema DEBE rechazarlo sin avisar, y después de 3 intentos el
   mensaje DEBE quedar en la cola `.dlq` de su cola.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| Los textos de los mensajes son los de REQ-1.1 y REQ-1.2 | REQ-1.1, REQ-1.2 | Solo cambian los textos de `Aviso` |

## Cambios

- (sin cambios)
