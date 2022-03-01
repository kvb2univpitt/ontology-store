-- psql postgresql://postgres:demouser@localhost:5432/postgres -f users.sql

CREATE USER i2b2actdx_ont WITH PASSWORD 'demouser';
CREATE USER i2b2actpcori_ont WITH PASSWORD 'demouser';
CREATE USER i2b2actomp_ont WITH PASSWORD 'demouser';
CREATE USER i2b2actdx_crc WITH PASSWORD 'demouser';
CREATE USER i2b2actpcori_crc WITH PASSWORD 'demouser';
CREATE USER i2b2actomp_crc WITH PASSWORD 'demouser';

GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actdx_ont;
GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actpcori_ont;
GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actomp_ont;
GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actdx_crc;
GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actpcori_crc;
GRANT ALL PRIVILEGES ON DATABASE i2b2 TO i2b2actomp_crc;
