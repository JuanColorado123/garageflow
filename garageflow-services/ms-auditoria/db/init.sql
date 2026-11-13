SET NAMES utf8mb4;

CREATE TABLE historial_estados (
  id              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  evento_id       VARCHAR(36)  NOT NULL,
  orden_id        VARCHAR(36)  NOT NULL,
  estado_anterior VARCHAR(20)  NULL,
  estado_nuevo    VARCHAR(20)  NOT NULL,
  motivo          VARCHAR(500) NULL,
  usuario         VARCHAR(100) NOT NULL,
  origen          VARCHAR(20)  NOT NULL,
  ocurrido_en     DATETIME(3)  NOT NULL,
  registrado_en   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT uk_historial_evento UNIQUE (evento_id),
  INDEX ix_historial_orden (orden_id, ocurrido_en)
);

CREATE TRIGGER historial_sin_update BEFORE UPDATE ON historial_estados FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'historial_estados es de solo insercion';

CREATE TRIGGER historial_sin_delete BEFORE DELETE ON historial_estados FOR EACH ROW
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'historial_estados es de solo insercion';
