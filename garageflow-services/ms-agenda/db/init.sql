SET NAMES utf8mb4;

CREATE TABLE asesores (
  id              INT         NOT NULL PRIMARY KEY,
  nombre          VARCHAR(80) NOT NULL,
  cupo_por_franja INT         NOT NULL,
  CONSTRAINT ck_asesores_cupo CHECK (cupo_por_franja > 0)
);

INSERT INTO asesores (id, nombre, cupo_por_franja) VALUES (1, 'Ana Gómez', 2), (2, 'Luis Pérez', 2);

CREATE TABLE citas (
  id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
  orden_id           VARCHAR(36)  NOT NULL,
  asesor_id          INT          NOT NULL,
  franja_inicio      DATETIME(3)  NOT NULL,
  tipo               VARCHAR(10)  NOT NULL,
  estado             VARCHAR(12)  NOT NULL,
  motivo_cancelacion VARCHAR(20)  NULL,
  cliente_nombre     VARCHAR(120) NOT NULL,
  cliente_telefono   VARCHAR(30)  NOT NULL,
  creada_en          DATETIME(3)  NOT NULL,
  cancelada_en       DATETIME(3)  NULL,
  CONSTRAINT fk_citas_asesor FOREIGN KEY (asesor_id) REFERENCES asesores (id),
  CONSTRAINT ck_citas_tipo CHECK (tipo IN ('TRAER','RECOGER')),
  CONSTRAINT ck_citas_estado CHECK (estado IN ('PENDIENTE','CANCELADA')),
  INDEX ix_citas_asesor_franja (asesor_id, franja_inicio, estado),
  INDEX ix_citas_orden (orden_id, estado)
);

CREATE TABLE outbox (
  id           VARCHAR(36)   NOT NULL PRIMARY KEY,
  routing_key  VARCHAR(60)   NOT NULL,
  payload      VARCHAR(4000) NOT NULL,
  creado_en    DATETIME(3)   NOT NULL,
  publicado_en DATETIME(3)   NULL,
  INDEX ix_outbox_pendientes (publicado_en, creado_en)
);

CREATE TABLE eventos_procesados (
  evento_id    VARCHAR(36)   NOT NULL PRIMARY KEY,
  procesado_en DATETIME(3)   NOT NULL
);
