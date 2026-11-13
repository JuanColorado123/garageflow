# Requisitos: 06-taller-web

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: las pantallas de `garageflow-web` para registrar y mover órdenes, agendar y cancelar
  citas, y ver el historial de auditoría.
- Decisiones que aplica: D-13, D-14; desde el 2026-11-12 también D-23 y D-24.
- Fuera de alcance de este spec: inicio de sesión (D-14), un diseño visual elaborado, pruebas de
  punta a punta automáticas en el navegador y la administración de asesores.
  Nota (2026-11-12): el diseño visual entra al alcance con REQ-6, por pedido del Developer (D-23).

## Glosario

- **Detalle de la orden**: la página `/ordenes/{id}`, con los datos de la orden, sus acciones, sus
  citas y su historial.

## Requisitos

### REQ-1 — Lista y registro de órdenes

**Historia:** Como asesor, quiero ver las órdenes del taller y registrar una nueva desde la
pantalla, para no usar Excel.

**Origen:** enunciado

**Criterios de aceptación**

1. CUANDO el usuario abre `/ordenes`, la página DEBE mostrar una tabla con el cliente, la placa,
   la aseguradora (o "Particular"), el estado y un enlace al detalle de cada orden, en el orden en
   que las devuelve la API.
2. CUANDO el usuario completa el formulario "Nueva orden" y lo envía, la página DEBE registrar la
   orden por la API y mostrarla en la tabla sin recargar la página.
3. SI la API rechaza el registro, ENTONCES la página DEBE mostrar el `detail` y el `codigo` de la
   respuesta y, junto a cada campo, el mensaje de `errores` que le corresponde.
4. El formulario DEBE marcar como obligatorios el nombre y el teléfono del cliente, la placa y el
   trabajo, y enviar lo que el usuario escribió para que decida la API.

### REQ-2 — Detalle y acciones de la orden

**Historia:** Como asesor, quiero ver una orden y hacer solo las acciones que el sistema permite.

**Origen:** enunciado, decisión D-13

**Criterios de aceptación**

1. CUANDO el usuario abre `/ordenes/{id}`, la página DEBE mostrar los datos de la orden, su estado
   y su origen.
2. La página DEBE mostrar el botón "Avanzar a <siguienteEstado>" solo si `accionesPermitidas`
   incluye `AVANZAR`, y el formulario "Reabrir" solo si incluye `REABRIR`.
3. CUANDO el usuario escribe un motivo y reabre, la página DEBE enviar el motivo a la API y mostrar
   la orden actualizada.
4. SI la API rechaza una acción, ENTONCES la página DEBE mostrar el `detail` y el `codigo` de la
   respuesta, por ejemplo `MOTIVO_REQUERIDO` si el motivo está vacío.
5. CUANDO una acción termina, la página DEBE volver a cargar la orden, sus citas y su historial.
6. La página DEBE tener un botón "Actualizar" que vuelve a cargar la orden, sus citas y su
   historial, para ver los cambios que llegan por eventos.

### REQ-3 — Citas de la orden

**Historia:** Como asesor, quiero agendar y cancelar las citas de la orden desde su detalle.

**Origen:** enunciado

**Criterios de aceptación**

1. El detalle de la orden DEBE listar sus citas con la fecha y hora, el tipo, el asesor, el estado
   y el motivo de cancelación.
2. CUANDO el usuario elige una fecha en "Agendar cita", la página DEBE mostrar las franjas de esa
   fecha que la API informa con al menos 1 cupo libre, con su asesor y la cantidad de libres.
3. CUANDO el usuario elige una franja y un tipo (`TRAER` o `RECOGER`) y agenda, la página DEBE
   crear la cita por la API y mostrarla en la lista.
4. La página DEBE mostrar el botón "Cancelar" solo en las citas cuyas `accionesPermitidas`
   incluyen `CANCELAR`, y CUANDO el usuario lo usa, DEBE cancelar la cita por la API y mostrarla
   como `CANCELADA`.
5. SI la API rechaza la cita o la cancelación, ENTONCES la página DEBE mostrar el `detail` y el
   `codigo`, por ejemplo `SIN_CUPO` u `ORDEN_CERRADA`.

### REQ-4 — Historial de auditoría

**Historia:** Como auditor, quiero ver quién cambió cada estado y cuándo.

**Origen:** enunciado

**Criterios de aceptación**

1. El detalle de la orden DEBE mostrar su historial con la fecha y hora, el usuario, el estado
   anterior, el estado nuevo y el motivo de cada cambio.
2. CUANDO el usuario abre `/auditoria`, la página DEBE mostrar los últimos cambios de todas las
   órdenes, con la orden, la fecha y hora, el usuario, los estados y el motivo.

### REQ-5 — Accesibilidad básica

**Historia:** Como usuario de teclado o lector de pantalla, quiero usar todas las pantallas.

**Origen:** `sobacol-architecture.md` §7

**Criterios de aceptación**

1. Cada campo de formulario DEBE tener una etiqueta asociada con `<label for>`, y cada mensaje de
   error de un campo DEBE estar ligado a ese campo con `aria-describedby`.
2. Los mensajes de error generales DEBEN tener `role="alert"`.
3. Todas las acciones DEBEN poder usarse con teclado, con botones y enlaces nativos y el foco
   visible.

### REQ-6 — Diseño visual y flujo de la orden

**Historia:** Como asesor, quiero pantallas claras, con la etapa de cada orden a la vista, para
saber en qué va cada vehículo y qué sigue.

**Origen:** decisión D-23 (pedido del Developer, 2026-11-12)

**Criterios de aceptación**

1. El sistema DEBE mostrar en todas las pantallas una barra superior con el nombre "GarageFlow",
   los enlaces "Órdenes" y "Auditoría", con el enlace de la pantalla actual resaltado, y el
   selector de usuario.
2. CUANDO el usuario abre `/ordenes`, la página DEBE mostrar arriba cuántas órdenes de la lista
   están en cada uno de los 5 estados.
3. El sistema DEBE mostrar los estados de orden, los estados y tipos de cita, los motivos de
   cancelación y el origen con un texto legible (por ejemplo, `EN_DIAGNOSTICO` como "En
   diagnóstico") y los estados con un color por estado. Esto incluye el botón "Avanzar a
   <siguienteEstado>" de REQ-2.2.
4. CUANDO el usuario abre `/ordenes/{id}`, la página DEBE mostrar el flujo de la orden con las 5
   etapas en orden, las anteriores al estado actual como completadas y el estado actual
   resaltado, con las acciones permitidas debajo. El flujo solo informa: las acciones siguen
   saliendo de `accionesPermitidas`.
5. El detalle de la orden DEBE mostrar su historial como una línea de tiempo, con los datos de
   REQ-4.1 y en el orden en que lo devuelve la API.
6. Todas las pantallas DEBEN usar las clases comunes de `src/styles.css` (`card`, `btn`, `input`,
   `badge`, `tabla`). En una ventana de 375 px de ancho, los formularios DEBEN quedar en una sola
   columna y las tablas DEBEN desplazarse dentro de su tarjeta.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| El asesor agenda las citas desde el detalle de la orden; no hay una pantalla de agenda general | REQ-3 | Habría una página más con las citas por fecha |

## Cambios

- 2026-11-12: se agrega REQ-6 (diseño visual y flujo de la orden) y el diseño visual sale de
  "Fuera de alcance". Lo pidió el Developer (D-23).
