# GarageFlow

Sistema para gestionar órdenes de servicio, citas y auditoría en un taller automotriz. Órdenes en estados, citas con cupo por asesor, avisos por WhatsApp simulados, e historial de cambios inalterable.

## Requisitos

Docker Desktop ejecutándose.

## Levantar

```powershell
docker compose up -d --build
```

Primera vez: varios minutos (descarga dependencias). El contenedor `datos-ejemplo` carga automáticamente órdenes y citas de ejemplo en las bases vacías. Abre http://localhost:4200.

Ver el progreso: `docker compose logs datos-ejemplo`. Recargar datos: `docker compose run --rm datos-ejemplo`. Forzar datos extras: `docker compose run --rm -e FORZAR=1 datos-ejemplo`.

Detener: `docker compose down`. Borrar los datos y empezar de cero: `docker compose down -v` y luego `docker compose up -d`; los datos de ejemplo se vuelven a cargar porque las bases quedan vacías.

## Usuarios y roles

| Usuario | Rol | Permisos |
|---|---|---|
| asesor.ana, asesor.luis | ASESOR | Órdenes y citas; historial de cada orden |
| auditor.marta | AUDITOR | Solo consulta; ve la página Auditoría |
| admin.sofia | ADMIN | Todo |

Cambiar usuario en el selector de la barra superior. Los roles se simulan con la cabecera `X-Rol`.

## Cómo funciona

**garageflow-web** (Angular, puerto 4200) llama a:

- **ms-ordenes** (8081): órdenes y cambios de estado
- **ms-agenda** (8082): citas y cupo por asesor
- **ms-auditoria** (8083): historial (solo lectura)
- **ms-notificaciones** (8084): avisos WhatsApp simulados; ver con `docker compose logs -f ms-notificaciones`

Cada servicio tiene su MySQL. Se comunican con eventos de RabbitMQ (consola en http://localhost:15672, usuario taller/taller); cada cambio de estado queda en la auditoría. Flujo: crear orden → cambiar estados → agendar citas → cerrar orden cancela citas pendientes (sin avisar al cliente).

## Pruebas

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-salud.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-ordenes.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-auditoria.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-cupo.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-cierre.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\prueba-roles.ps1
```

Cada script termina con `RESULTADO: TODOS PASS` si funciona.

## Estructura

```
garageflow/
├── docker-compose.yml
├── README.md (este archivo)
├── DECISIONES.md (decisiones de diseño)
├── .kiro/specs/ (requisitos, diseño y tareas por característica)
├── garageflow-web/ (frontend Angular)
├── garageflow-services/
│   ├── ms-ordenes/
│   ├── ms-agenda/
│   ├── ms-auditoria/
│   └── ms-notificaciones/
├── seed/ (datos de ejemplo)
└── scripts/ (pruebas)
```

URLs: **Frontend** http://localhost:4200 • **RabbitMQ** http://localhost:15672 (taller/taller) • **APIs** http://localhost:8081/actuator/health, 8082/actuator/health, 8083/actuator/health, 8084/actuator/health • **DECISIONES.md** [diseño del proyecto](./DECISIONES.md).
