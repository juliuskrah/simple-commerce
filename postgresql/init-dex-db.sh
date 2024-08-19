#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE USER dex_user WITH PASSWORD 'yVweto4e';
  CREATE DATABASE dex;
  GRANT ALL PRIVILEGES ON DATABASE dex TO dex_user;
  \c dex
  GRANT USAGE, CREATE ON SCHEMA public TO dex_user;
EOSQL
