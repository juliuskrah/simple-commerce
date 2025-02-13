#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE USER dex_user WITH PASSWORD 'yVweto4e';
  CREATE DATABASE dex;
  CREATE USER keto_user WITH PASSWORD 'H75NgxE83umbtT4aQGLpYk';
  CREATE DATABASE keto;
  GRANT ALL PRIVILEGES ON DATABASE dex TO dex_user;
  GRANT ALL PRIVILEGES ON DATABASE keto TO keto_user;
  \c dex
  GRANT USAGE, CREATE ON SCHEMA public TO dex_user;
  \c keto
  GRANT USAGE, CREATE ON SCHEMA public TO keto_user;
EOSQL
