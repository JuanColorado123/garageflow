#!/bin/sh
set -e
mysql --protocol=socket -uroot -p"$MYSQL_ROOT_PASSWORD" <<SQL
CREATE USER IF NOT EXISTS 'auditoria_app'@'%' IDENTIFIED BY '${AUDITORIA_APP_PASSWORD}';
GRANT SELECT, INSERT ON auditoria_db.historial_estados TO 'auditoria_app'@'%';
SQL
