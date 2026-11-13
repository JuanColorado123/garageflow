# Requisitos: 00-base

Estado: APROBADO (2026-11-12, Developer)

## Alcance

- Capacidad: levantar y probar todo el sistema con `docker compose up --build`, y las
  convenciones que comparten los microservicios y el frontend (errores, CORS, usuario simulado).
- Decisiones que aplica: D-01, D-06, D-12, D-13, D-14, D-16, D-18, D-21.
- Fuera de alcance de este spec: las reglas de negocio (specs `01` a `05`) y las pantallas
  (spec `06`). La autenticación real (D-14, "Fuera de alcance" de `DECISIONES.md`).

## Glosario

- **Microservicio con API**: `ms-ordenes`, `ms-agenda` y `ms-auditoria`, que reciben llamadas
  del navegador. `ms-notificaciones` solo escucha eventos.
- **Problem details**: respuesta de error JSON con tipo de contenido `application/problem+json`
  y los campos `status`, `detail` y `codigo`.

## Requisitos

### REQ-1 — Levantar todo con un comando

**Historia:** Como Developer, quiero levantar el sistema con un solo comando, para probarlo sin
instalar Java 21 ni Node 22 en mi máquina.

**Origen:** enunciado ("todo levantando con docker compose up"), decisión D-18

**Criterios de aceptación**

1. CUANDO el Developer ejecuta `docker compose up --build -d` en `c:\repos_cobis\Prueba`, el
   sistema DEBE construir las imágenes y dejar en ejecución los contenedores `rabbitmq`,
   `mysql-ordenes`, `mysql-agenda`, `mysql-auditoria`, `mysql-notificaciones`, `ms-ordenes`,
   `ms-agenda`, `ms-auditoria`, `ms-notificaciones` y `garageflow-web`.
2. CUANDO se construye la imagen de un microservicio o del frontend, el sistema DEBE compilarlo
   dentro de Docker y, en los microservicios, correr sus pruebas unitarias.
3. SI una prueba unitaria falla, ENTONCES la construcción de la imagen de ese microservicio DEBE
   fallar.
4. MIENTRAS su base de datos o RabbitMQ no pasen su chequeo de salud, Docker Compose DEBE esperar
   antes de arrancar el microservicio.
5. CUANDO todos los contenedores están en ejecución, el frontend DEBE responder en
   `http://localhost:4200` y la consola de RabbitMQ en `http://localhost:15672`.
6. CUANDO un contenedor MySQL arranca con su volumen vacío, DEBE crear sus tablas con los scripts
   de la carpeta `db/` de su microservicio. Los microservicios no crean ni modifican tablas.
   (Origen: decisión D-12)

### REQ-2 — Salud de los microservicios

**Historia:** Como Developer, quiero saber si cada microservicio está listo, para encontrar rápido
el que falla.

**Origen:** `sobacol-architecture.md` §8

**Criterios de aceptación**

1. CUANDO se consulta `GET /actuator/health` en `ms-ordenes` (puerto 8081), `ms-agenda` (8082),
   `ms-auditoria` (8083) o `ms-notificaciones` (8084), el microservicio DEBE responder 200 con
   `"status":"UP"` si su base de datos y RabbitMQ responden.

### REQ-3 — Errores con un formato común

**Historia:** Como usuario del frontend, quiero ver la razón exacta de un rechazo, para corregir
lo que envié.

**Origen:** decisión D-13 ("la interfaz muestra los errores que devuelve la API")

**Criterios de aceptación**

1. SI una petición REST se rechaza por una regla de negocio, ENTONCES el microservicio DEBE
   responder problem details con el estado HTTP de la regla, un `detail` legible y el `codigo` de
   la regla.
2. SI el cuerpo de la petición no es JSON válido o tiene campos inválidos, ENTONCES el
   microservicio DEBE responder 400 con `codigo` `DATOS_INVALIDOS` y, si hay campos inválidos, un
   objeto `errores` con el nombre de cada campo y su mensaje.
3. SI ocurre un error inesperado, ENTONCES el microservicio DEBE responder 500 con `codigo`
   `ERROR_INTERNO`, sin mostrar detalles internos en el `detail`.

### REQ-4 — Llamadas desde el navegador

**Historia:** Como usuario, quiero que la página llame directo a los microservicios, sin un
servidor intermedio.

**Origen:** decisión D-13

**Criterios de aceptación**

1. CUANDO el navegador con origen `http://localhost:4200` llama a un microservicio con API, el
   microservicio DEBE aceptar la llamada por CORS, incluida la cabecera `X-Usuario`.
2. CUANDO el frontend arranca, DEBE leer las URLs de los microservicios del archivo
   `/config.json`, sin URLs escritas en el código de `src/app`.

### REQ-6 — Datos de ejemplo al levantar

**Historia:** Como Developer, quiero que al levantar el sistema en otro equipo las bases ya tengan
datos, para mostrarlo sin crearlos a mano.

**Origen:** decisión D-26 (pedido del Developer, 2026-11-12)

**Criterios de aceptación**

1. CUANDO se ejecuta `docker compose up` con las bases vacías, el sistema DEBE crear, por la API,
   al menos 6 órdenes de ejemplo en estados distintos (incluida una `CERRADA`, una reabierta con
   motivo y al menos una con aseguradora) y al menos 3 citas, e imprimir
   `DATOS_EJEMPLO: listo` en el log de `datos-ejemplo`.
2. SI ya existe al menos una orden, ENTONCES `datos-ejemplo` NO DEBE crear datos e DEBE imprimir
   `DATOS_EJEMPLO: ya hay datos`.
3. SI un microservicio no responde `UP` en 5 minutos, ENTONCES `datos-ejemplo` DEBE terminar con
   código distinto de 0 e imprimir qué microservicio faltó.

### REQ-5 — Usuario simulado

**Historia:** Como asesor, quiero elegir quién soy en la pantalla, para que la auditoría registre
quién hizo cada cambio.

**Origen:** decisión D-14 (supuesto)

**Criterios de aceptación**

1. El frontend DEBE mostrar en la barra superior un selector de usuario con `asesor.ana`,
   `asesor.luis` y `auditor.marta`, con `asesor.ana` elegido al abrir la página.
   Nota (2026-11-12): `07-roles` agrega `admin.sofia` y el rol de cada usuario (D-25).
2. CUANDO el frontend llama a un microservicio, DEBE enviar la cabecera `X-Usuario` con el
   usuario elegido en ese momento.

## Supuestos por confirmar

| Supuesto | Criterios afectados | Qué cambia si es falso |
|---|---|---|
| Los usuarios de demostración son `asesor.ana`, `asesor.luis` y `auditor.marta` | REQ-5.1 | Solo cambia la lista del frontend |
| Las contraseñas de MySQL y RabbitMQ tienen valores de demostración en `docker-compose.yml`, que se cambian con un archivo `.env` | REQ-1.1 | Habría que crear `.env` antes de `docker compose up` |
| La zona horaria del taller es `America/Bogota` | REQ-1.1 | Cambia la variable `TZ` de los contenedores |

## Cambios

- 2026-11-12: se agrega REQ-6, datos de ejemplo (D-26), pedido por el Developer. Las rutas
  `services/...` ahora son `garageflow-services/...` (D-27).
