-- PostgreSQL schema for production
CREATE TABLE IF NOT EXISTS tasks
 (
   id BIGSERIAL NOT NULL PRIMARY KEY,
   summary VARCHAR(256) NOT NULL,
   description TEXT,
   status VARCHAR(256) NOT NULL
 );
