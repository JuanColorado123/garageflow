SET NAMES utf8mb4;

CREATE TABLE ordenes (
  id                   VARCHAR(36)   NOT NULL PRIMARY KEY,
  cliente_nombre       VARCHAR(120)  NOT NULL,
  cliente_telefono     VARCHAR(30)   NOT NULL,
  vehiculo_placa       VARCHAR(15)   NOT NULL,
  vehiculo_descripcion VARCHAR(120)  NULL,
  aseguradora          VARCHAR(120)  NULL,
  trabajo              VARCHAR(1000) NOT NULL,
  origen               VARCHAR(20)   NOT NULL,
  estado               VARCHAR(20)   NOT NULL,
  creada_en            DATETIME(3)   NOT NULL,
  actualizada_en       DATETIME(3)   NOT NULL,
  version              BIGINT        NOT NULL DEFAULT 0,
  CONSTRAINT ck_ordenes_estado CHECK (estado IN ('REGISTRADA','EN_DIAGNOSTICO','EN_REPARACION','REPARADA','CERRADA')),
  CONSTRAINT ck_ordenes_origen CHECK (origen IN ('ASESOR','ASEGURADORA'))
);

CREATE TABLE outbox (
  id           VARCHAR(36)   NOT NULL PRIMARY KEY,
  routing_key  VARCHAR(60)   NOT NULL,
  payload      VARCHAR(4000) NOT NULL,
  creado_en    DATETIME(3) NOT NULL,
  publicado_en DATETIME(3) NULL,
  INDEX ix_outbox_pendientes (publicado_en, creado_en)
);
