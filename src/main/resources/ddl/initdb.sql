CREATE DATABASE imgzone
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Japanese_Japan.932'
    LC_CTYPE = 'Japanese_Japan.932'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE TABLE account
(
    username varchar(100) NOT NULL,
    password varchar (100) NOT NULL,
    email varchar (100),
    authority varchar(10) NOT NULL,
    createdt date NOT NULL,
    enabled boolean NOT NULL,
    CONSTRAINT account_pkey PRIMARY KEY (username)
);

CREATE TABLE folder
(
    username varchar(100) NOT NULL,
    seq int NOT NULL,
    name varchar(100),
    status varchar(10) NOT NULL,
    guest varchar(100),
    expiredt date,
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

CREATE TABLE photo
(
    username varchar(100) NOT NULL,
    seq int NOT NULL,
    file varchar(100) NOT NULL,
    price int NOT NULL,
    createdt date NOT NULL,
    CONSTRAINT photo_pkey PRIMARY KEY (username, seq, file)
);


--passwd:1234
INSERT INTO account(
    username, password, email, authority, createdt, enabled)
    VALUES ('admin', '$2a$10$u150rp5eJSHI7t/T9Y1Jne4rdZPxzkelqtyplkb25LSmjZIVplDi.', NULL, 'ADMIN', '2018-07-01', true);
INSERT INTO account(
    username, password, email, authority, createdt, enabled)
    VALUES ('user', '$2a$10$u150rp5eJSHI7t/T9Y1Jne4rdZPxzkelqtyplkb25LSmjZIVplDi.', 'user@mail.com', 'USER', '2018-07-01', true);
INSERT INTO folder(username, status)
	VALUES ('user', 'Free'), ('user', 'Free'), ('user', 'Free');
INSERT INTO account(
    username, password, email, authority, createdt, enabled)
    VALUES ('guest', '$2a$10$u150rp5eJSHI7t/T9Y1Jne4rdZPxzkelqtyplkb25LSmjZIVplDi.', NULL, 'GUEST', '2018-07-01', true);


