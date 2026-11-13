# Requisitos: 01-ordenes

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: registrar órdenes de servicio, moverlas por sus estados, reabrirlas con motivo y
  avisar cada cambio a los demás microservicios.
- Decisiones que aplica: D-02, D-04, D-07, D-08, D-11, D-14, D-21.
- Fuera de alcance de este spec: la integración real con aseguradoras (D-11), editar o borrar
  órdenes, y la gestión de clientes y vehículos ("Fuera de alcance" de `DECISIONES.md`).

## Glosario

- **Orden**: orden de servicio de un vehículo en el taller.
- **Particular**: orden de un cliente sin aseguradora.
- **Origen**: quién creó la orden: `ASESOR` (por la pantalla) o `ASEGURADORA` (por la
  integración futura).

## Requisitos

### REQ-1 — Registrar una orden

**Historia:** Como asesor, quiero registrar la orden con el cliente, el vehículo, la aseguradora
si aplica y el trabajo a realizar, para dejar de llevarla en Excel.

**Origen:** enunciado

**Criterios de aceptación**

1. CUANDO el asesor envía el nombre y el teléfono del cliente, la placa del vehículo, el trabajo a
   realizar y, si quiere, la descripción del vehículo y la aseguradora, el sistema DEBE crear la
   orden en estado `REGISTRADA` con origen `ASESOR` y responder 201 con la orden.
2. SI falta el nombre del cliente, su teléfono, la placa o el trabajo, ENTONCES el sistema DEBE
   responder 400 `DATOS_INVALIDOS` sin crear la orden.
3. CUANDO la orden llega sin aseguradora, el sistema DEBE guardarla con `aseguradora` vacía, como
   orden particular.

### REQ-2 — Avanzar por los estados

**Historia:** Como asesor, quiero avanzar la orden por sus estados, para que todos sepan en qué va
la reparación.

**Origen:** enunciado; la lista de estados es un supuesto (D-02)

**Criterios de aceptación**

1. CUANDO el asesor avanza una orden, el sistema DEBE pasarla al estado siguiente de
   `REGISTRADA → EN_DIAGNOSTICO → EN_REPARACION → REPARADA → CERRADA` y responder 200 con la orden.
2. SI la orden está `CERRADA` y se pide avanzarla, ENTONCES el sistema DEBE responder 409
   `TRANSICION_INVALIDA` sin cambiarla.
3. SI la orden no existe, ENTONCES el sistema DEBE responder 404 `ORDEN_NO_ENCONTRADA`.
4. SI otro usuario cambió el estado de la orden entre la lectura y la escritura, ENTONCES el
   sistema DEBE responder 409 `ORDEN_MODIFICADA` sin aplicar el cambio.

### REQ-3 — Reabrir con motivo

**Historia:** Como asesor, quiero reabrir una orden reparada o cerrada indicando el motivo, para
retomar el trabajo cuando el vehículo vuelve.

**Origen:** enunciado ("a veces hay que reabrirla, y siempre con una razón"); los estados desde
los que se reabre son un supuesto (D-02)

**Criterios de aceptación**

1. CUANDO el asesor reabre una orden `REPARADA` o `CERRADA` con un motivo, el sistema DEBE pasarla
   a `EN_REPARACION` y responder 200 con la orden.
2. SI el motivo falta o está vacío, ENTONCES el sistema DEBE responder 400 `MOTIVO_REQUERIDO` sin
   cambiar la orden.
3. SI la orden está en `REGISTRADA`, `EN_DIAGNOSTICO` o `EN_REPARACION`, ENTONCES el sistema DEBE
   responder 409 `TRANSICION_INVALIDA` sin cambiarla.

### REQ-4 — Quién hace cada cambio

**Historia:** Como auditor, quiero que cada cambio lleve el usuario que lo hizo, para saber quién
cambió cada estado.

**Origen:** enunciado (auditoría), decisión D-14

**Criterios de aceptación**

1. SI una petición que registra, avanza o reabre una orden no trae la cabecera `X-Usuario`, o la
   trae vacía, ENTONCES el sistema DEBE responder 400 `USUARIO_REQUERIDO` sin cambiar nada.

### REQ-5 — Consultar órdenes

**Historia:** Como asesor, quiero ver las órdenes y lo que puedo hacer con cada una, para no
intentar cambios que el sistema no permite.

**Origen:** decisión D-13 (la interfaz muestra solo las acciones que permite la API)

**Criterios de aceptación**

1. CUANDO se consulta la lista de órdenes, el sistema DEBE devolverlas todas, de la más reciente a
   la más antigua.
2. CUANDO se consulta una orden, el sistema DEBE incluir `estado`, `siguienteEstado` (vacío si la
   orden está `CERRADA`) y `accionesPermitidas`: `AVANZAR` si la orden no está `CERRADA`, y
   `REABRIR` si está `REPARADA` o `CERRADA`.

### REQ-6 — Avisar los cambios a los demás microservicios

**Historia:** Como auditoría y como agenda, quiero enterarme de cada cambio de estado, para
registrarlo y para cancelar las citas cuando la orden se cierra.

**Origen:** decisiones D-04, D-07 y D-08

**Criterios de aceptación**

1. CUANDO una orden se registra, avanza o se reabre, el sistema DEBE publicar
   `EstadoOrdenCambiado` versión 1 con `ordenId`, `estadoAnterior` (vacío al registrar),
   `estadoNuevo`, `motivo` (solo al reabrir), `usuario`, `origen` y `ocurridoEn`.
2. CUANDO una orden pasa a `CERRADA`, el sistema DEBE publicar además `OrdenCerrada` versión 1 con
   `ordenId`, `usuario` y `ocurridoEn`.
3. El sistema DEBE guardar el cambio de la orden y sus eventos en la misma transacción: o se
   guardan los dos, o ninguno.
4. SI RabbitMQ no está disponible cuando ocurre el cambio, ENTONCES el sistema DEBE aceptar el
   cambio y publicar sus eventos cuando RabbitMQ vuelva.

### REQ-7 — Origen de la orden, listo para las aseguradoras

**Historia:** Como taller, quiero que las órdenes de las aseguradoras grandes puedan llegar
después por integración, sin rehacer el registro.

**Origen:** enunciado, decisión D-11

**Criterios de aceptación**

1. CUANDO el asesor registra una orden por la API, el sistema DEBE guardarla con origen `ASESOR`.
2. CUANDO el caso de uso de registro recibe origen `ASEGURADORA`, el sistema DEBE crear la orden
   con ese origen y con las mismas reglas que las del asesor.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| Los estados son `REGISTRADA`, `EN_DIAGNOSTICO`, `EN_REPARACION`, `REPARADA` y `CERRADA`, y se avanza de uno en uno | REQ-2.1, REQ-2.2, REQ-5.2 | La tabla de transiciones de `EstadoOrden` |
| Se reabre desde `REPARADA` o `CERRADA`, y la orden vuelve a `EN_REPARACION` | REQ-3.1, REQ-3.3, REQ-5.2 | El método `reabrir` de `Orden` |

## Cambios

- (sin cambios)
