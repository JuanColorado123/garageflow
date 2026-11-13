# Diseño: 05-cierre-orden

Estado: APROBADO (2026-11-12, Developer)

## Resumen

`ms-agenda` escucha `OrdenCerrada`. En una transacción registra el `eventoId` como procesado,
cancela con motivo `CIERRE_ORDEN` las citas `PENDIENTE` de la orden y guarda un `CitaCancelada`
por cada una en el outbox. `ms-notificaciones` lee el motivo en el evento y omite el aviso: lo
decide el dato que publica `ms-agenda`, no una consulta a otro microservicio (D-08).

Cubre: REQ-1, REQ-2.

## Decisiones aplicadas

- D-07: consumidor idempotente (`eventos_procesados`) y outbox.
- D-08: el motivo `CIERRE_ORDEN` viaja en `CitaCancelada` y decide que no se avise.
- Convenciones de `00-base`.
- Decisiones nuevas: ninguna.

## Proyectos afectados

| Proyecto | Qué cambia | Datos propios que toca |
|---|---|---|
| `services/ms-agenda` | Consumidor de `OrdenCerrada` y caso de uso de cierre | `citas`, `eventos_procesados`, `outbox` |
| `services/ms-notificaciones` | Regla para omitir el aviso | `avisos` |

## Ubicación hexagonal

`ms-agenda` (paquete base `com.subacol.msagenda`):

| Clase o archivo | Capa | Responsabilidad |
|---|---|---|
| `OrdenCerradaListener` | `adapter.in.messaging` | `@RabbitListener` de `agenda.orden-cerrada`; lee el texto con `JsonMapper` y llama a `alCerrarseOrden` |
| `OrdenCerradaV1` | `adapter.in.messaging` | DTO con `eventoId` y `ordenId`; ignora el resto |
| `AgendaUseCase` | `application.port.in` | Agrega `alCerrarseOrden(eventoId, ordenId)` |
| `EventosProcesados` | `application.port.out` | `boolean registrarSiEsNuevo(eventoId)` |
| `CitaRepository` | `application.port.out` | Agrega `List<Integer> asesoresConPendientes(ordenId)` y `List<Cita> pendientesDeOrdenParaActualizar(ordenId)` (bloquea las citas y lee su estado actual) |
| `AgendaService` | `application.service` | Implementa `alCerrarseOrden` con `@Transactional` |
| `EventoProcesadoEntity`, `EventoProcesadoJpaRepository` | `adapter.out.persistence` | Tabla `eventos_procesados` |
| `EventosProcesadosPersistenceAdapter` | `adapter.out.persistence` | Implementa `EventosProcesados`: si `existsById(eventoId)`, devuelve `false`; si no, guarda y devuelve `true` |
| `CitaJpaRepository` | `adapter.out.persistence` | Agrega `@Query("select distinct c.asesorId from CitaEntity c where c.ordenId = :ordenId and c.estado = :estado order by c.asesorId")` y `findByOrdenIdAndEstado(ordenId, estado)` con `@Lock(PESSIMISTIC_WRITE)` |
| `CitaPersistenceAdapter` | `adapter.out.persistence` | Implementa los dos métodos nuevos de `CitaRepository` |
| `RabbitConfig` | `config` | Cola `agenda.orden-cerrada` con DLQ, enlazada al exchange `taller.eventos` con la routing key `orden.cerrada` |
| `db/init.sql` | — | Tabla `eventos_procesados` |

`ms-notificaciones` (paquete base `com.subacol.msnotificaciones`):

| Clase | Capa | Responsabilidad |
|---|---|---|
| `Aviso` | `domain` | `porCitaCancelada(..., CIERRE_ORDEN)` deja el aviso `OMITIDO`, sin mensaje |
| `AvisosService` | `application.service` | Si el aviso nuevo está `OMITIDO`, escribe `WHATSAPP_OMITIDO cita=<citaId> motivo=CIERRE_ORDEN` en el log y no llama a `Notificador` |

## Caso de uso

`alCerrarseOrden(eventoId, ordenId)`:

1. Si `eventos.registrarSiEsNuevo(eventoId)` devuelve `false`, termina (evento repetido).
2. `asesoresConPendientes(ordenId)` y `asesores.buscarYBloquear(id)` para cada uno, de menor a
   mayor `id` (regla de bloqueo del spec `03`).
3. Para cada cita de `pendientesDeOrdenParaActualizar(ordenId)`: `cita.cancelar(CIERRE_ORDEN,
   ahora)`, `guardar(cita)` y publica sus eventos.
4. Las citas que ya estaban `CANCELADA` no aparecen en `pendientesDeOrdenParaActualizar`, así que
   no cambian (REQ-1.4).

Todo ocurre en una transacción: si algo falla, no queda el `eventoId` registrado y el mensaje se
reintenta.

## Contratos

### Eventos

| Evento y versión | Publica | Consume | Campos que usa |
|---|---|---|---|
| `OrdenCerrada` v1 | `ms-ordenes` | `ms-agenda` | `eventoId`, `ordenId` |
| `CitaCancelada` v1 con `motivo` `CIERRE_ORDEN` | `ms-agenda` | `ms-notificaciones` | los de spec `04` |

Cola `agenda.orden-cerrada`, enlazada a `taller.eventos` con `orden.cerrada`, con dead letter a
`agenda.orden-cerrada.dlq`.

## Modelo de datos

Se agrega a `services/ms-agenda/db/init.sql`:

```sql
CREATE TABLE eventos_procesados (
  evento_id    VARCHAR(36) NOT NULL PRIMARY KEY,
  procesado_en DATETIME(3) NOT NULL
);
```

`ms-notificaciones` usa la tabla `avisos` del spec `04`, con estado `OMITIDO`.

## Flujo

```mermaid
sequenceDiagram
  participant A as Asesor
  participant O as ms-ordenes
  participant MQ as RabbitMQ
  participant G as ms-agenda
  participant N as ms-notificaciones
  A->>O: avanzar (REPARADA -> CERRADA)
  O->>MQ: OrdenCerrada (outbox)
  MQ->>G: OrdenCerrada
  G->>G: registra eventoId; cancela citas PENDIENTE con CIERRE_ORDEN
  G->>MQ: CitaCancelada motivo=CIERRE_ORDEN (outbox, una por cita)
  MQ->>N: CitaCancelada
  N->>N: Aviso OMITIDO; log WHATSAPP_OMITIDO; sin WhatsApp
```

## Errores y casos límite

| Caso | Respuesta | Criterio |
|---|---|---|
| `OrdenCerrada` repetido | Se descarta por `eventos_procesados` | REQ-1.3 |
| La orden no tiene citas pendientes | Se registra el evento y no cambia nada | REQ-1.1 |
| Una cita se cancela a mano mientras llega el cierre | Las dos operaciones toman primero el bloqueo del asesor; si gana la manual, el cierre lee la cita ya `CANCELADA` y no la toca: conserva `MANUAL` y no se publica un segundo `CitaCancelada` | REQ-1.4 |
| `ms-agenda` está caído cuando se cierra la orden | `OrdenCerrada` espera en su cola y se procesa al volver | REQ-1.1 |
| La orden se reabre después del cierre | Las citas canceladas siguen canceladas (supuesto) | — |

## Estrategia de pruebas

| Criterio | Prueba | Tipo |
|---|---|---|
| REQ-1.1 | `AgendaServiceTest`: "REQ-1.1 OrdenCerrada cancela las citas pendientes con CIERRE_ORDEN"; `scripts/prueba-cierre.ps1` | caso de uso, manual |
| REQ-1.2 | `AgendaServiceTest`: "REQ-1.2 por cada cita cancelada se publica CitaCancelada con CIERRE_ORDEN"; `scripts/prueba-cierre.ps1`: la línea `WHATSAPP_OMITIDO` de A muestra que llegó su `CitaCancelada` con `CIERRE_ORDEN` | caso de uso, manual |
| REQ-1.3 | `AgendaServiceTest`: "REQ-1.3 un OrdenCerrada repetido se procesa una sola vez" | caso de uso |
| REQ-1.4 | `AgendaServiceTest`: "REQ-1.4 las citas ya canceladas no cambian"; `scripts/prueba-cierre.ps1`: la cita cancelada a mano conserva `MANUAL` | caso de uso, manual |
| REQ-2.1 | `AvisoTest`: "REQ-2.1 una cancelación por CIERRE_ORDEN no se envía"; `AvisosServiceTest`: "REQ-2.1 una cancelación por CIERRE_ORDEN no llama al notificador"; `scripts/prueba-cierre.ps1`: no hay una segunda línea `WHATSAPP_SIMULADO` para la cita | dominio, caso de uso, manual |
| REQ-2.2 | `AvisosServiceTest`: "REQ-2.2 el aviso omitido queda registrado como OMITIDO"; `scripts/prueba-cierre.ps1` encuentra `WHATSAPP_OMITIDO cita=<id>` | caso de uso, manual |

## Riesgos

- Una cita agendada justo mientras se cierra la orden puede quedar `PENDIENTE` (riesgo aceptado
  en D-06).

## Cambios

- 2026-11-12: persistencia con Spring Data JPA y Hibernate (D-21), pedida por el Developer al
  aprobar.
