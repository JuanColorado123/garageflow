# Diseño: 06-taller-web

Estado: APROBADO (2026-11-12, Developer)

## Resumen

Tres rutas con carga diferida: `/ordenes` (lista y registro), `/ordenes/:id` (detalle con
acciones, citas e historial) y `/auditoria` (últimos cambios). Los componentes son standalone,
guardan su estado en signals y llaman a la API solo por los servicios de `data-access`. La página
muestra lo que la API permite y los errores que la API devuelve; no decide reglas (D-13).

Cubre: REQ-1, REQ-2, REQ-3, REQ-4, REQ-5 y REQ-6.

## Decisiones aplicadas

- D-13: llamadas directas a los 3 microservicios con API, con las URLs de `/config.json`.
- D-14: `usuarioInterceptor` agrega `X-Usuario` (spec `00-base`).
- Convenciones de `00-base`: `ConfiguracionService`, `UsuarioService`, `mensajeDeError`.
- Decisiones nuevas: ninguna.

## Proyectos afectados

| Proyecto | Qué cambia | Datos propios que toca |
|---|---|---|
| `garageflow-web` | Rutas, páginas, componentes y servicios de `data-access` | — |

## Ubicación

Bajo `garageflow-web/src/app`:

| Archivo | Capa | Responsabilidad |
|---|---|---|
| `app.routes.ts` | raíz | `''` → `ordenes`; `ordenes`, `ordenes/:id` y `auditoria` con `loadComponent` |
| `features/ordenes/data-access/ordenes.models.ts` | data-access | `Orden`, `RegistrarOrden`, `AccionOrden` (de `OrdenResponse` del spec `01`) |
| `features/ordenes/data-access/ordenes.api.ts` | data-access | `OrdenesApi`: `listar`, `obtener`, `registrar`, `avanzar`, `reabrir` |
| `features/ordenes/pages/ordenes-page.component.ts` | página | Tabla de órdenes y formulario "Nueva orden" |
| `features/ordenes/pages/orden-detalle-page.component.ts` | página | Datos, "Avanzar", "Reabrir", "Actualizar"; contiene las citas y el historial |
| `features/agenda/data-access/agenda.models.ts` | data-access | `Cita`, `Asesor`, `Disponibilidad`, `AgendarCita` (spec `03`) |
| `features/agenda/data-access/agenda.api.ts` | data-access | `AgendaApi`: `asesores`, `disponibilidad`, `citasDeOrden`, `agendar`, `cancelar` |
| `features/agenda/components/citas-orden.component.ts` | componente | Entrada `ordenId`; lista de citas, formulario "Agendar cita", botón "Cancelar"; avisa con una salida `cambio` |
| `features/auditoria/data-access/auditoria.models.ts` | data-access | `CambioDeEstado` (spec `02`) |
| `features/auditoria/data-access/auditoria.api.ts` | data-access | `AuditoriaApi`: `historial(ordenId?)` |
| `features/auditoria/components/historial-tabla.component.ts` | componente de presentación | Entradas `cambios` y `mostrarOrden`; solo dibuja la tabla |
| `features/auditoria/pages/auditoria-page.component.ts` | página | Carga los últimos cambios y usa `historial-tabla` |
| `src/styles.css` | global | Estilos simples de tabla y formulario, y foco visible |

- Plantillas en línea (`template:`) para no multiplicar archivos.
- Formularios reactivos (`ReactiveFormsModule`).
- Los servicios de `data-access` arman las URLs con `ConfiguracionService`
  (`ordenesUrl + '/api/ordenes'`, etc.). Ningún componente usa `HttpClient`.
- Fechas con `DatePipe` y el formato `dd/MM/yyyy HH:mm`.

## Contratos que usa

| Pantalla | Llamadas |
|---|---|
| `/ordenes` | `GET /api/ordenes`, `POST /api/ordenes` (spec `01`) |
| `/ordenes/:id` | `GET /api/ordenes/{id}`, `POST .../avanzar`, `POST .../reabrir` (spec `01`); `GET /api/agenda/citas?ordenId=`, `GET /api/agenda/asesores`, `GET /api/agenda/disponibilidad?fecha=`, `POST /api/agenda/citas`, `POST /api/agenda/citas/{id}/cancelar` (spec `03`); `GET /api/auditoria/historial?ordenId=` (spec `02`) |
| `/auditoria` | `GET /api/auditoria/historial` (spec `02`) |

## Comportamiento de las pantallas

- `/ordenes`: al entrar carga la lista. El formulario tiene `clienteNombre`, `clienteTelefono`,
  `vehiculoPlaca`, `vehiculoDescripcion`, `aseguradora` y `trabajo`; los 4 obligatorios llevan `*`
  y `aria-required="true"`. El botón "Registrar" siempre envía; si la API responde con error, se
  muestra `mensajeDeError` arriba del formulario y cada mensaje de `errores` junto a su campo. Si
  responde bien, la orden nueva se agrega arriba de la tabla y el formulario se limpia.
- `/ordenes/:id`: carga la orden, sus citas y su historial. "Avanzar a <siguienteEstado>" y el
  formulario "Reabrir" (campo `motivo` y botón) aparecen según `accionesPermitidas`. Después de
  cada acción, y con el botón "Actualizar", vuelve a cargar las 3 cosas.
- Citas: la lista muestra la fecha y hora, el tipo, el nombre del asesor (de `asesores`), el
  estado y el motivo. "Agendar cita" tiene `fecha` (`<input type="date">`), `franja` (un `<select>`
  con las franjas de `disponibilidad` que tienen `libres > 0`, como "10:00 — Ana Gómez (1 libre)")
  y `tipo` (`TRAER` o `RECOGER`). "Cancelar" aparece en las citas cuyas `accionesPermitidas`
  incluyen `CANCELAR`.
- `/auditoria`: carga `historial()` sin orden y lo muestra con `historial-tabla` (con la columna
  de la orden).

## Errores y casos límite

| Caso | Respuesta | Criterio |
|---|---|---|
| La API no responde | "No hay conexión con el servidor" (`mensajeDeError`) | REQ-1.3, REQ-2.4, REQ-3.5 |
| Reabrir con el motivo vacío | Se envía igual y se muestra `MOTIVO_REQUERIDO` | REQ-2.4 |
| La franja se llenó entre la consulta y la reserva | Se muestra `SIN_CUPO` y se vuelve a cargar la disponibilidad | REQ-3.5 |
| La cita se cancela por el cierre unos segundos después | Aparece como `CANCELADA` / `CIERRE_ORDEN` al usar "Actualizar" | REQ-2.6 |

## Estrategia de pruebas

Las pruebas del frontend se corren en la máquina del Developer con Chrome; las demás son
revisiones manuales en `http://localhost:4200` con el sistema levantado.

| Criterio | Prueba | Tipo |
|---|---|---|
| REQ-1.1 | Abrir `/ordenes` y comparar la tabla con `GET /api/ordenes` | manual |
| REQ-1.2 | Registrar una orden desde el formulario; aparece arriba de la tabla | manual |
| REQ-1.3 | Registrar sin trabajo; se ven `DATOS_INVALIDOS` y el mensaje junto a "Trabajo" | manual |
| REQ-1.4 | Los 4 campos tienen `*` y `aria-required`; el botón envía aunque estén vacíos | manual |
| REQ-2.1 | Abrir el detalle de una orden; se ven sus datos, estado y origen | manual |
| REQ-2.2 | `orden-detalle-page.component.spec.ts`: `it('06 REQ-2.2 muestra solo las acciones permitidas por la API')` | unitaria (frontend, local) |
| REQ-2.3 | Reabrir una orden `CERRADA` con motivo; queda `EN_REPARACION` | manual |
| REQ-2.4 | Reabrir sin motivo; se ve `MOTIVO_REQUERIDO` | manual |
| REQ-2.5 | Después de avanzar, el historial muestra el cambio nuevo sin recargar el navegador | manual |
| REQ-2.6 | Cerrar una orden con una cita pendiente y usar "Actualizar"; la cita aparece `CANCELADA` | manual |
| REQ-3.1 | El detalle muestra las citas con sus 5 datos | manual |
| REQ-3.2 | Elegir una fecha; el `<select>` muestra solo franjas con libres | manual |
| REQ-3.3 | Agendar; la cita aparece en la lista | manual |
| REQ-3.4 | "Cancelar" solo en citas `PENDIENTE`; al usarlo queda `CANCELADA` | manual |
| REQ-3.5 | Agendar en una orden `CERRADA`; se ve `ORDEN_CERRADA` | manual |
| REQ-4.1 | El detalle muestra el historial de la orden | manual |
| REQ-4.2 | `/auditoria` muestra los últimos cambios con la columna de la orden | manual |
| REQ-5.1 | Revisar las plantillas: cada `<input>`, `<select>` y `<textarea>` tiene `<label for>`, y cada error de campo tiene `aria-describedby` | revisión estática |
| REQ-5.2 | Los errores generales tienen `role="alert"` | revisión estática |
| REQ-5.3 | Recorrer cada pantalla con Tab, Enter y Espacio; el foco se ve | manual |

## Diseño visual (REQ-6, D-23, D-24)

- Tailwind CSS 4.3: `tailwindcss`, `@tailwindcss/postcss` y `postcss` en `devDependencies`;
  `.postcssrc.json` con el plugin `@tailwindcss/postcss`; `src/styles.css` empieza con
  `@import "tailwindcss";` y define las clases comunes en `@layer components`.
- Sin SSR (D-24): `angular.json` sin `server`, `outputMode`, `ssr` ni `security`;
  `tsconfig.app.json` solo con `src/main.ts`; `ConfiguracionService.cargar()` ya no revisa la
  plataforma.
- Los componentes no tienen `styles`. Las clases se escriben completas en las plantillas o en
  `etiquetas.ts`, porque Tailwind no encuentra clases armadas con texto.

| Archivo | Capa | Responsabilidad |
|---|---|---|
| `shared/ui/etiquetas.ts` | presentación | Textos y colores de los valores de la API, y `FLUJO_ORDEN` (las 5 etapas en el orden en que se dibujan) |
| `features/ordenes/components/flujo-orden.component.ts` | componente de presentación | Entrada `estado`; dibuja las 5 etapas |
| `features/auditoria/components/historial-linea-tiempo.component.ts` | componente de presentación | Entrada `cambios`; dibuja la línea de tiempo del detalle |

- Barra superior en `app.component.ts`; el contenido va centrado, con ancho máximo.
- `/ordenes`: 5 contadores por estado. Debajo van el formulario (un tercio) y la tabla (dos
  tercios) en pantallas grandes, y uno sobre otro en las angostas.
- `/ordenes/{id}`: cabecera con cliente, placa e insignia de estado; tarjeta "Flujo de la orden"
  con `flujo-orden` y las acciones debajo. En pantallas grandes, las citas y la línea de tiempo
  van a la izquierda y los datos a la derecha.
- `historial-tabla` queda solo para `/auditoria`, con un enlace al detalle de cada orden.

| Criterio | Prueba | Tipo |
|---|---|---|
| REQ-6.1 | Abrir las 3 pantallas; la barra muestra el nombre, los 2 enlaces con el actual resaltado y el selector | manual |
| REQ-6.2 | En `/ordenes`, los 5 contadores suman el total de la tabla | manual |
| REQ-6.3 | Tabla, detalle, citas e historial muestran textos legibles; el botón dice, por ejemplo, "Avanzar a En diagnóstico" | manual |
| REQ-6.4 | Detalle de una orden `EN_REPARACION`: 2 etapas completadas, la tercera resaltada y "Avanzar a Reparada" debajo | manual |
| REQ-6.5 | El detalle muestra la línea de tiempo con fecha, usuario, estados y motivo | manual |
| REQ-6.6 | Las plantillas usan las clases comunes y no tienen `styles`; a 375 px, formularios en una columna y tablas con desplazamiento | revisión estática y manual |

## Riesgos

- Sin pruebas automáticas en el navegador, una regresión de pantalla se ve solo al revisar a mano.
  Queda en "Fuera de alcance" de `DECISIONES.md`.

## Cambios

- 2026-11-12: se agregan "Diseño visual" y sus pruebas (REQ-6, D-23), y se quita el SSR (D-24).
  El detalle usa `historial-linea-tiempo` en lugar de `historial-tabla`. Lo pidió el Developer.
