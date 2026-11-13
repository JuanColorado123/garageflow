# Diseño: 00-base

Estado: APROBADO (2026-11-12, Developer)

## Resumen

`docker-compose.yml` levanta RabbitMQ, 4 MySQL, los 4 microservicios y el frontend. Cada proyecto
tiene un Dockerfile de dos etapas: la primera compila y corre las pruebas unitarias, la segunda
solo ejecuta (D-18). Este spec también fija las convenciones que usan los demás: dependencias,
paquetes, errores, eventos, CORS y usuario simulado.

Cubre: REQ-1, REQ-2, REQ-3, REQ-4, REQ-5.

## Decisiones aplicadas

- D-01: 4 microservicios y RabbitMQ.
- D-06: eventos por RabbitMQ; la convención de eventos está en "Eventos y RabbitMQ".
- D-12: cada MySQL ejecuta los scripts de `db/` de su microservicio la primera vez que arranca.
- D-13: el navegador llama directo a cada microservicio con API; CORS en cada uno.
- D-14: cabecera `X-Usuario`.
- D-16: se usan los esqueletos de `c:\repos_cobis\Prueba`.
- D-21: Spring Data JPA con Hibernate; las tablas las crean los scripts y Hibernate las valida al
  arrancar (`ddl-auto=validate`).
- D-18: compilar y probar dentro de Docker.
- Decisiones nuevas: ninguna.

## Prerrequisitos

- Docker Desktop en ejecución y el comando `docker` disponible en la terminal.
- No hace falta Java ni Node en la máquina.

## Proyectos afectados

| Proyecto | Qué cambia | Datos propios que toca |
|---|---|---|
| `services/ms-ordenes` | Dependencias, configuración, Dockerfile, manejo de errores, CORS, RabbitMQ | Ninguno (las tablas llegan en `01`) |
| `services/ms-agenda` | Lo mismo, con Gradle | Ninguno (las tablas llegan en `03`) |
| `services/ms-auditoria` | Lo mismo | Ninguno (las tablas llegan en `02`) |
| `services/ms-notificaciones` | Dependencias, Java 21, configuración, Dockerfile, RabbitMQ | Ninguno (las tablas llegan en `04`) |
| `garageflow-web` | Dockerfile, renderizado en el navegador, configuración, usuario simulado, estructura de la página | — |
| Raíz de `c:\repos_cobis\Prueba` | `docker-compose.yml`, `.env.example`, `.gitignore`, `README.md`, `scripts/prueba-salud.ps1` | — |

## Contenedores

| Contenedor | Imagen o carpeta | Puerto en la máquina | Arranca cuando estén sanos |
|---|---|---|---|
| `rabbitmq` | `rabbitmq:4-management` | 15672 (consola) | — |
| `mysql-ordenes` | `mysql:8.4` | — | — |
| `mysql-agenda` | `mysql:8.4` | — | — |
| `mysql-auditoria` | `mysql:8.4` | — | — |
| `mysql-notificaciones` | `mysql:8.4` | — | — |
| `ms-ordenes` | `./services/ms-ordenes` | 8081 | `mysql-ordenes`, `rabbitmq` |
| `ms-agenda` | `./services/ms-agenda` | 8082 | `mysql-agenda`, `rabbitmq` |
| `ms-auditoria` | `./services/ms-auditoria` | 8083 | `mysql-auditoria`, `rabbitmq` |
| `ms-notificaciones` | `./services/ms-notificaciones` | 8084 | `mysql-notificaciones`, `rabbitmq` |
| `garageflow-web` | `./garageflow-web` | 4200 | arranca después de `ms-ordenes`, `ms-agenda` y `ms-auditoria` (sin esperar salud) |

- Chequeo de salud de MySQL: `mysqladmin ping -h 127.0.0.1 -uroot -p$$MYSQL_ROOT_PASSWORD --silent`.
  Usa TCP (`-h 127.0.0.1`) a propósito: mientras corre los scripts de `db/`, MySQL no abre el
  puerto 3306, así que el chequeo pasa recién cuando los scripts terminaron.
- Chequeo de salud de RabbitMQ: `rabbitmq-diagnostics -q ping`.
- Los microservicios tienen `restart: on-failure`.
- Cada MySQL monta `./services/<ms>/db` en `/docker-entrypoint-initdb.d` (solo lectura) y guarda
  sus datos en un volumen con nombre (`datos-ordenes`, `datos-agenda`, `datos-auditoria`,
  `datos-notificaciones`). `docker compose down -v` borra los volúmenes y vuelve a correr los
  scripts en el siguiente arranque.

### Variables

`docker-compose.yml` usa `${VARIABLE:-valor}` con valores de demostración, para que
`docker compose up` funcione sin crear nada. `.env.example` lista las mismas variables; quien
quiera otros valores copia el archivo a `.env`, que está en `.gitignore`.

| Variable | Valor de demostración | La usan |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `root` | los 4 MySQL |
| `ORDENES_DB_PASSWORD` | `ordenes` | `mysql-ordenes` (`MYSQL_PASSWORD`), `ms-ordenes` |
| `AGENDA_DB_PASSWORD` | `agenda` | `mysql-agenda`, `ms-agenda` |
| `AUDITORIA_APP_PASSWORD` | `auditoria` | `mysql-auditoria` (la usa `db/usuario-app.sh`, spec `02`), `ms-auditoria` |
| `NOTIFICACIONES_DB_PASSWORD` | `notificaciones` | `mysql-notificaciones`, `ms-notificaciones` |
| `RABBITMQ_USER` / `RABBITMQ_PASSWORD` | `taller` / `taller` | `rabbitmq` (`RABBITMQ_DEFAULT_USER`, `RABBITMQ_DEFAULT_PASS`) y los 4 microservicios |
| `TZ` | `America/Bogota` | los 4 microservicios |

- Bases y usuarios: `ordenes_db` con usuario `ordenes`, `agenda_db` con `agenda`,
  `notificaciones_db` con `notificaciones` (variables `MYSQL_DATABASE`, `MYSQL_USER` y
  `MYSQL_PASSWORD` de la imagen). `auditoria_db` no define `MYSQL_USER`, porque esa imagen le da
  todos los permisos; su usuario `auditoria_app` lo crea el spec `02` con permisos limitados.
- RabbitMQ necesita un usuario propio: el usuario `guest` de la imagen solo acepta conexiones
  desde el mismo contenedor.

Variables de cada microservicio en `docker-compose.yml` (Spring Boot las lee sin código):

| Variable | Valor |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysql-<ms>:3306/<db>?sslMode=DISABLED&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | usuario y contraseña de su base |
| `SPRING_RABBITMQ_HOST` | `rabbitmq` |
| `SPRING_RABBITMQ_USERNAME` / `SPRING_RABBITMQ_PASSWORD` | `${RABBITMQ_USER}` / `${RABBITMQ_PASSWORD}` |
| `APP_CORS_ORIGEN` | `http://localhost:4200` (solo los 3 con API) |
| `APP_ORDENES_URL` | `http://ms-ordenes:8081` (solo `ms-agenda`, spec `03`) |
| `TZ` | `${TZ}` |
| `JAVA_TOOL_OPTIONS` | `-Xmx256m` |

## Dockerfiles

Maven (`ms-ordenes`, `ms-auditoria`, `ms-notificaciones`); cambia solo el puerto de `EXPOSE`:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Nota (2026-11-12, D-22): `ms-agenda` ya no usa este Dockerfile; usa el de Maven con `EXPOSE 8082`.

Gradle (`ms-agenda`). `build.gradle` desactiva la tarea `jar` para que solo quede el jar
ejecutable en `build/libs`:

```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src
RUN --mount=type=cache,target=/root/.gradle sh ./gradlew --no-daemon test bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Angular (`garageflow-web`):

```dockerfile
FROM node:22-slim
ENV NG_CLI_ANALYTICS=false
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Compila una vez para que un error de TypeScript haga fallar la construcción de la imagen.
RUN npx ng build --configuration development
EXPOSE 4200
CMD ["npx", "ng", "serve", "--host", "0.0.0.0", "--port", "4200"]
```

Cada proyecto tiene un `.dockerignore` que excluye `target`, `build`, `.gradle`, `.idea`,
`node_modules`, `dist`, `.angular` y `.git`.

## Dependencias

Nombres que genera start.spring.io para Spring Boot 4.1.2. Reemplazan a `spring-boot-starter`;
`spring-boot-starter-test` se queda para las pruebas.

| Dependencia | ordenes | agenda | auditoria | notificaciones |
|---|---|---|---|---|
| `spring-boot-starter-webmvc` | Sí | Sí | Sí | Sí (solo para `/actuator/health`) |
| `spring-boot-starter-validation` | Sí | Sí | Sí | No |
| `spring-boot-starter-data-jpa` | Sí | Sí | Sí | Sí |
| `spring-boot-starter-amqp` | Sí | Sí | Sí | Sí |
| `spring-boot-starter-actuator` | Sí | Sí | Sí | Sí |
| `com.mysql:mysql-connector-j` (runtime) | Sí | Sí | Sí | Sí |

`ms-notificaciones` pasa `java.version` de 17 a 21.

## Configuración

`src/main/resources/application.properties` de cada microservicio (sin contraseñas):

```properties
spring.application.name=<ms>
server.port=<puerto>
spring.datasource.url=jdbc:mysql://localhost:3306/<db>?sslMode=DISABLED&allowPublicKeyRetrieval=true
spring.datasource.username=<usuario>
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
management.endpoints.web.exposure.include=health
app.cors.origen=http://localhost:4200
```

Los que escuchan eventos (`ms-agenda`, `ms-auditoria`, `ms-notificaciones`) agregan:

```properties
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1s
```

## Convenciones de código

Paquetes bajo el paquete base de cada microservicio (`com.subacol.msordenes`, ...):

| Paquete | Qué va | Qué no puede usar |
|---|---|---|
| `domain` | Entidades, objetos de valor, eventos de dominio, `ErrorDeNegocio` | Spring, `jakarta.*`, Jackson, Lombok |
| `application.port.in` | Interfaz del caso de uso y sus comandos | SQL, HTTP, RabbitMQ |
| `application.port.out` | Interfaces de repositorio, publicador, notificador, clientes REST | SQL, HTTP, RabbitMQ |
| `application.service` | Implementación del caso de uso, con `@Service` y `@Transactional` | SQL, HTTP, RabbitMQ |
| `adapter.in.web` | Controladores REST, DTO de entrada y salida, `ManejadorErrores` | Repositorios directos |
| `adapter.in.messaging` | Consumidores de eventos y sus DTO | Otros adaptadores |
| `adapter.out.persistence` | Entidades JPA (`*Entity`), repositorios Spring Data (`*JpaRepository`) y el adaptador que implementa el puerto (`*PersistenceAdapter`) | Otros adaptadores |
| `adapter.out.messaging` | `OutboxEntity`, `OutboxJpaRepository`, `OutboxPublicador`, `OutboxRelay` y DTO de eventos | Otros adaptadores |
| `adapter.out.http` | Clientes REST de otros microservicios | Otros adaptadores |
| `adapter.out.notification` | Envío de avisos | Otros adaptadores |
| `config` | `AppConfig`, `CorsConfig`, `RabbitConfig` | — |

- La hora se toma de un bean `java.time.Clock` (`Clock.systemDefaultZone()`, en `AppConfig`), para
  poder fijarla en las pruebas.
- Los ids de órdenes, citas y eventos son UUID en texto (`VARCHAR(36)`), generados por el
  microservicio dueño.
- Mapeo JPA (D-21), para que `ddl-auto=validate` pase:
  - Entidades con `@Entity`, `@Table` y `@Column` con los nombres de los scripts; constructor sin
    argumentos `protected`. Nunca salen del adaptador: el adaptador las traduce a clases del dominio.
  - `String` ↔ `VARCHAR(n)` con `@Column(length = n)`; ids UUID con `length = 36`.
  - Enumeraciones ↔ `VARCHAR` con `@Enumerated(EnumType.STRING)` (Hibernate 7 acepta `VARCHAR` al
    validar un enum).
  - `LocalDateTime` ↔ `DATETIME(3)`; `Integer` ↔ `INT`; `Long` ↔ `BIGINT`.
  - Los métodos de los repositorios son derivados del nombre siempre que se pueda; `@Query` (JPQL)
    solo para proyecciones y agrupaciones.
- Las fechas viajan como texto ISO-8601 sin zona (`2026-11-13T10:00:00`), en la zona `TZ` del
  taller.
- Pruebas: JUnit 5 sin contexto de Spring, en `src/test/java/<paquete base>/domain` y
  `.../application`, con puertos falsos escritos en la misma prueba. El nombre de cada prueba
  empieza con el número del spec y el criterio que demuestra, porque `ms-agenda` y
  `ms-notificaciones` tienen pruebas de varios specs: `@DisplayName("01 REQ-3.2 reabrir sin motivo
  es rechazado")` en Java e `it('00 REQ-5.2 agrega X-Usuario ...')` en Angular. Las tablas de
  pruebas de cada spec omiten el número del spec.

## Ubicación hexagonal

| Clase o archivo | Proyecto | Capa | Responsabilidad |
|---|---|---|---|
| `ErrorDeNegocio` | los 4 | `domain` | Excepción de regla con `tipo` (`INVALIDO`, `NO_ENCONTRADO`, `CONFLICTO`, `NO_DISPONIBLE`) y `codigo` |
| `ManejadorErrores` | ordenes, agenda, auditoria | `adapter.in.web` | `@RestControllerAdvice` que devuelve problem details |
| `AppConfig` | los 4 | `config` | Bean `Clock`; `@EnableScheduling` en ordenes y agenda (outbox) |
| `CorsConfig` | ordenes, agenda, auditoria | `config` | `WebMvcConfigurer`: `/api/**`, origen `app.cors.origen`, métodos `GET` y `POST`, todas las cabeceras |
| `RabbitConfig` | los 4 | `config` | Declara el exchange `taller.eventos`; cada spec agrega sus colas |
| `Dockerfile`, `.dockerignore` | los 5 | — | Construcción en dos etapas |
| `db/init.sql` | los 4 | — | Encabezado; cada spec agrega sus tablas |
| `ConfiguracionService` | garageflow-web | `core/config` | Carga `/config.json` al arrancar |
| `UsuarioService` | garageflow-web | `core/usuario` | Lista de usuarios y usuario elegido (signal) |
| `usuarioInterceptor` | garageflow-web | `core/usuario` | Agrega `X-Usuario` a cada petición |
| `mensajeDeError` | garageflow-web | `core/errores` | Saca `detail` y `codigo` de un problem details |
| `AppComponent` | garageflow-web | raíz | Barra superior con navegación y selector de usuario |

## Errores

`ManejadorErrores` traduce:

| Excepción | Estado | `codigo` |
|---|---|---|
| `ErrorDeNegocio` con tipo `INVALIDO` | 400 | el de la regla |
| `ErrorDeNegocio` con tipo `NO_ENCONTRADO` | 404 | el de la regla |
| `ErrorDeNegocio` con tipo `CONFLICTO` | 409 | el de la regla |
| `ErrorDeNegocio` con tipo `NO_DISPONIBLE` | 503 | el de la regla |
| `MethodArgumentNotValidException` | 400 | `DATOS_INVALIDOS`, con `errores` campo → mensaje |
| `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, `MissingServletRequestParameterException`, `HandlerMethodValidationException` | 400 | `DATOS_INVALIDOS` |
| Excepciones de Spring MVC que implementan `ErrorResponse` (ruta inexistente, método no permitido) | el suyo | — |
| Cualquier otra | 500 | `ERROR_INTERNO`; el detalle va al log, no a la respuesta |

Ejemplo:

```json
{ "status": 409, "title": "Conflict", "detail": "El asesor no tiene cupo en esa franja", "codigo": "SIN_CUPO" }
```

## Eventos y RabbitMQ

- Exchange `taller.eventos`, tipo topic, durable. Lo declaran todos los microservicios.
- Cada evento es un JSON con `eventoId` (UUID), `tipo` (nombre del evento), `version` (`1`),
  `ocurridoEn` y los campos del evento. Se envía como texto (`text/plain`, UTF-8) con
  `RabbitTemplate.convertAndSend`, y se arma y lee con el `JsonMapper` que configura Spring Boot.
  Cada consumidor tiene su propio DTO con `@JsonIgnoreProperties(ignoreUnknown = true)`.
- Publicación con outbox (D-07), en `ms-ordenes` y `ms-agenda`:
  - Tabla `outbox (id VARCHAR(36) PK, routing_key VARCHAR(60), payload VARCHAR(4000), creado_en
    DATETIME(3), publicado_en DATETIME(3) NULL)`. `id` es el `eventoId`.
  - `OutboxPublicador` guarda una `OutboxEntity` con `OutboxJpaRepository`, dentro de la
    transacción del caso de uso.
  - `OutboxRelay`, con `@Scheduled(fixedDelay = 1000)` y `@Transactional`, lee hasta 50 filas
    pendientes (`findTop50ByPublicadoEnIsNullOrderByCreadoEnAsc()`), las envía y marca
    `publicado_en`. Si un envío falla, corta la vuelta y lo reintenta en la siguiente.
- Consumo (D-07): cada cola es durable y, después de 3 intentos fallidos, el mensaje pasa a la
  cola `<cola>.dlq` (dead letter por el exchange por defecto). Cada consumidor descarta los
  eventos repetidos por `eventoId`.

| Evento | Routing key | Cola | Consumidor | Spec |
|---|---|---|---|---|
| `EstadoOrdenCambiado` | `orden.estado-cambiado` | `auditoria.orden-estado-cambiado` | `ms-auditoria` | `01`, `02` |
| `OrdenCerrada` | `orden.cerrada` | `agenda.orden-cerrada` | `ms-agenda` | `01`, `05` |
| `CitaAgendada` | `cita.agendada` | `notificaciones.cita-agendada` | `ms-notificaciones` | `03`, `04` |
| `CitaCancelada` | `cita.cancelada` | `notificaciones.cita-cancelada` | `ms-notificaciones` | `03`, `04`, `05` |

## Frontend base

- Nota (2026-11-12, D-24): se quitó el SSR; `app.routes.server.ts`, `app.config.server.ts`,
  `main.server.ts` y `server.ts` ya no existen, y `ConfiguracionService` no revisa la plataforma.
- `src/app/app.routes.server.ts`: `RenderMode.Client` para `**`. El navegador hace todo el
  renderizado; no hay prerenderizado ni SSR en ejecución.
- `src/app/app.config.ts`: `provideZoneChangeDetection({ eventCoalescing: true })`,
  `provideRouter(routes)`, `provideHttpClient(withInterceptors([usuarioInterceptor]))` y
  `provideAppInitializer(() => inject(ConfiguracionService).cargar())`. Se quita
  `provideClientHydration(withEventReplay())`, porque sin SSR no hay nada que hidratar.
- `ConfiguracionService.cargar()` no hace nada fuera del navegador
  (`isPlatformBrowser(inject(PLATFORM_ID))`). El CLI arranca la aplicación en Node para leer las
  rutas del servidor, y ahí no existe `/config.json`; sin esa guarda, `ng build` y `ng serve`
  fallarían.
- `public/config.json`:

  ```json
  { "ordenesUrl": "http://localhost:8081", "agendaUrl": "http://localhost:8082", "auditoriaUrl": "http://localhost:8083" }
  ```

- `AppComponent`: encabezado con el nombre "GarageFlow", enlaces "Órdenes" (`/ordenes`) y
  "Auditoría" (`/auditoria`), y el selector `<label for="usuario">Usuario</label>` +
  `<select id="usuario">`. Debajo, `<main>` con `<router-outlet>`. Las rutas llegan en el spec `06`.
- `mensajeDeError(error)`: si la respuesta trae `detail`, devuelve `detail (codigo)`; si el estado
  es 0, "No hay conexión con el servidor"; si no, "Ocurrió un error inesperado".

## Flujo

```mermaid
sequenceDiagram
  participant D as Developer
  participant C as docker compose
  participant M as MySQL y RabbitMQ
  participant S as Microservicios
  participant W as garageflow-web
  D->>C: docker compose up --build -d
  C->>C: construye imágenes (compila y corre pruebas)
  C->>M: arranca; MySQL corre los scripts de db/
  M-->>C: chequeos de salud OK
  C->>S: arranca los 4 microservicios
  C->>W: arranca ng serve en 4200
```

## Errores y casos límite

| Caso | Respuesta | Criterio |
|---|---|---|
| Una prueba unitaria falla durante `docker compose up --build` | La construcción se detiene con el error de la prueba | REQ-1.3 |
| MySQL todavía corre sus scripts | Los microservicios esperan; el chequeo por TCP no pasa | REQ-1.4 |
| Un microservicio arranca antes de que RabbitMQ acepte conexiones | Spring reintenta la conexión; si el proceso termina, `restart: on-failure` lo vuelve a arrancar | REQ-1.4 |
| Se cambia un script de `db/` con la base ya creada | No se aplica; hay que correr `docker compose down -v` (borra los datos) | REQ-1.6 |

## Estrategia de pruebas

| Criterio | Prueba | Tipo |
|---|---|---|
| REQ-1.1 | `docker compose up --build -d` y `docker compose ps`: los 10 contenedores en ejecución | manual |
| REQ-1.2 | En la salida de la construcción aparecen las pruebas de cada microservicio (`Tests run` en Maven, tarea `test` en Gradle) y el `ng build` del frontend | manual |
| REQ-1.3 | Los Dockerfiles corren `mvn package` y `gradlew test` sin `-DskipTests` ni `-x test` | revisión estática |
| REQ-1.4 | `depends_on` con `condition: service_healthy` y los chequeos de salud de "Contenedores" | revisión estática |
| REQ-1.5 | `scripts/prueba-salud.ps1`: `http://localhost:4200` y `http://localhost:15672` responden 200 | manual |
| REQ-1.6 | Cada microservicio tiene `spring.jpa.hibernate.ddl-auto=validate` y ningún `spring.sql.init`; `SHOW TABLES` en cada MySQL muestra las tablas de su `db/init.sql` | revisión estática y manual |
| REQ-2.1 | `scripts/prueba-salud.ps1`: los 4 `/actuator/health` responden `UP` | manual |
| REQ-3.1 | Los scripts de los specs `01` y `03` reciben `codigo` en los rechazos | manual |
| REQ-3.2 | `scripts/prueba-ordenes.ps1` (spec `01`) envía una orden sin `trabajo` y recibe 400 `DATOS_INVALIDOS` con `errores.trabajo` | manual |
| REQ-3.3 | `ManejadorErrores` responde `ERROR_INTERNO` sin el mensaje de la excepción | revisión estática |
| REQ-4.1 | `scripts/prueba-salud.ps1` envía una petición `OPTIONS` con `Origin: http://localhost:4200` a los 3 microservicios con API y recibe `Access-Control-Allow-Origin` | manual |
| REQ-4.2 | No hay `http://localhost` en `src/app`; con otro valor en `public/config.json`, las llamadas van a ese valor | revisión estática y manual |
| REQ-5.1 | `app.component.spec.ts`: `it('REQ-5.1 muestra el selector con los 3 usuarios y asesor.ana elegido')` | unitaria (frontend, local) |
| REQ-5.2 | `usuario.interceptor.spec.ts`: `it('REQ-5.2 agrega X-Usuario con el usuario elegido')` | unitaria (frontend, local) |

Las pruebas del frontend se corren en la máquina del Developer
(`npx ng test --watch=false --browsers=ChromeHeadless`), porque necesitan Chrome. Si no hay
Chrome, se revisan a mano en la página.

## Riesgos

- La primera construcción descarga dependencias de Maven, Gradle y npm, y tarda varios minutos.
  Mitigación: caché de BuildKit para `.m2` y `.gradle`.
- Memoria: 4 MySQL, RabbitMQ, 4 JVM y Node. Mitigación: `-Xmx256m` en cada JVM. Si Docker Desktop
  se queda corto, se pasa a un solo MySQL con un esquema por microservicio (D-12).
- `ng serve` es el servidor de desarrollo de Angular; alcanza para la demo (D-13).
- Si una entidad no coincide con su tabla, el microservicio no arranca (`validate`). Mitigación:
  las reglas de mapeo JPA de "Convenciones de código".

## Cambios

- 2026-11-12: persistencia con Spring Data JPA y Hibernate (D-21), pedida por el Developer al
  aprobar.
- 2026-11-12: `ms-agenda` usa el Dockerfile de Maven, con `EXPOSE 8082`, y deja de usar el de
  Gradle (D-22). Las menciones a Gradle de "Dockerfiles", "Estrategia de pruebas" (REQ-1.2 y
  REQ-1.3) y "Riesgos" quedan como historia; ahora los cuatro microservicios usan Maven.
- 2026-11-12: datos de ejemplo (REQ-6, D-26). Servicio `datos-ejemplo` en compose: imagen
  `node:22-slim`, monta `./seed` de solo lectura, corre `node /seed/seed.mjs` una vez
  (`restart: "no"`). Espera `/actuator/health` `UP` de `ms-ordenes`, `ms-agenda` y
  `ms-auditoria`; si `GET /api/ordenes` devuelve algo, termina. Si no, crea las órdenes y citas
  por la API con `X-Usuario` y `X-Rol: ASESOR`. Prueba: manual, logs de `datos-ejemplo` y
  conteo por la API. Las rutas `services/` pasan a `garageflow-services/` (D-27).
