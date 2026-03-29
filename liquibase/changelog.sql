--liquibase formatted sql

--changeset pushker.chaubey:initial_schema
CREATE TABLE IF NOT EXISTS user (
    username text,
    address text,
    PRIMARY KEY (username)
);
