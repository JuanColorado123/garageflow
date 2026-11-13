SET NAMES utf8mb4;

CREATE TABLE avisos (
  evento_id     VARCHAR(36)  NOT NULL PRIMARY KEY,
  cita_id       VARCHAR(36)  NOT NULL,
  orden_id      VARCHAR(36)  NOT NULL,
  tipo_evento   VARCHAR(20)  NOT NULL,
  telefono      VARCHAR(30)  NOT NULL,
  mensaje       VARCHAR(500) NULL,
  estado        VARCHAR(10)  NOT NULL,
  registrado_en DATETIME(3)  NOT NULL,
  CONSTRAINT ck_avisos_estado CHECK (estado IN ('ENVIADO','OMITIDO'))
);
