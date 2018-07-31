CREATE DATABASE imgzone
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Japanese_Japan.932'
    LC_CTYPE = 'Japanese_Japan.932'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

DROP TABLE IF EXISTS account;
CREATE TABLE account
(
    username varchar(100) NOT NULL,
    password varchar (100) NOT NULL,
    email varchar (100),
    authority varchar(10) NOT NULL,
    createdt timestamp NOT NULL,
    enabled boolean NOT NULL,
    expiredt date,
    logindt timestamp,
    CONSTRAINT account_pkey PRIMARY KEY (username)
);

DROP FUNCTION IF EXISTS get_folder_seq() CASCADE;
DROP TABLE IF EXISTS folder;
CREATE TABLE folder
(
    username varchar(100) NOT NULL,
    seq int NOT NULL,
    name varchar(100),
    locked boolean NOT NULL,
    shared boolean NOT NULL,
    guest varchar(100),
    createdt timestamp NOT NULL,
    CONSTRAINT folder_pkey PRIMARY KEY (username, seq)
);
CREATE INDEX folder_idx1 ON folder (guest);
CREATE FUNCTION get_folder_seq() RETURNS trigger AS $folder_insert$
BEGIN
    SELECT coalesce(max(seq), 0) + 1 INTO NEW.seq FROM folder WHERE username = NEW.username;
    RETURN NEW;
END;
$folder_insert$ LANGUAGE plpgsql;
CREATE TRIGGER folder_insert
    BEFORE INSERT ON folder FOR EACH ROW
    EXECUTE PROCEDURE get_folder_seq();

DROP TABLE IF EXISTS photo;
CREATE TABLE photo
(
    username varchar(100) NOT NULL,
    folder int NOT NULL,
    thumbnail varchar(100) NOT NULL,
    original varchar(100) NOT NULL,
    price int NOT NULL,
    createdt timestamp NOT NULL,
    CONSTRAINT photo_pkey PRIMARY KEY (username, folder, thumbnail)
);

DROP TABLE IF EXISTS order;
CREATE TABLE order
(
    number varchar(100) NOT NULL,
    username varchar(100) NOT NULL,
    folder int NOT NULL,
    thumbnail varchar(100) NOT NULL,
    original varchar(100) NOT NULL,
    price int NOT NULL,
    email varchar (100) NOT NULL,
    createdt timestamp NOT NULL,
    CONSTRAINT order_pkey PRIMARY KEY (number, username, folder, thumbnail)
);


--passwd:1234
INSERT INTO account(
    username, password, email, authority, createdt, enabled)
    VALUES ('admin', '$2a$10$u150rp5eJSHI7t/T9Y1Jne4rdZPxzkelqtyplkb25LSmjZIVplDi.', NULL, 'ADMIN', CURRENT_TIMESTAMP, true);
