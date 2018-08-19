DROP TABLE IF EXISTS account;
CREATE TABLE account
(
    username varchar(20) NOT NULL,
    password varchar (60) NOT NULL,
    email varchar (255),
    authority varchar(10) NOT NULL,
    balance int NOT NULL DEFAULT 0,
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
    username varchar(20) NOT NULL,
    seq int NOT NULL,
    name varchar(255),
    locked boolean NOT NULL,
    shared boolean NOT NULL,
    guest varchar(20),
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
    username varchar(20) NOT NULL,
    folder int NOT NULL,
    thumbnail varchar(50) NOT NULL,
    original varchar(50) NOT NULL,
    filename varchar(255) NOT NULL,
    price int NOT NULL,
    createdt timestamp NOT NULL,
    CONSTRAINT photo_pkey PRIMARY KEY (username, folder, thumbnail)
);

DROP TABLE IF EXISTS "order";
CREATE TABLE "order"
(
    orderno varchar(20) NOT NULL,
    email varchar (255) NOT NULL,
    username varchar(20) NOT NULL,
    folder int NOT NULL,
    thumbnail varchar(50) NOT NULL,
    original varchar(50) NOT NULL,
    filename varchar(255) NOT NULL,
    price int NOT NULL,
    createdt timestamp NOT NULL,
    charged boolean NOT NULL,
    expiredt date NOT NULL,
    CONSTRAINT order_pkey PRIMARY KEY (orderno, email, original)
);
CREATE INDEX order_idx1 ON "order" (username, folder, thumbnail);

DROP TABLE IF EXISTS transfer;
CREATE TABLE transfer
(
	transno varchar(20) NOT NULL,
    username varchar(20) NOT NULL,
    bank varchar(100) NOT NULL,
    branch varchar(100) NOT NULL,
    actype int NOT NULL,
    acnumber varchar(10) NOT NULL,
    acname varchar(100) NOT NULL,
    amount int NOT NULL,
    done boolean NOT NULL,
    createdt timestamp NOT NULL,
    updatedt timestamp,
    CONSTRAINT transfer_pkey PRIMARY KEY (transno)
);


--passwd:1234
INSERT INTO account(
    username, password, email, authority, createdt, enabled)
    VALUES ('admin', '$2a$10$u150rp5eJSHI7t/T9Y1Jne4rdZPxzkelqtyplkb25LSmjZIVplDi.', NULL, 'ADMIN', CURRENT_TIMESTAMP, true);
