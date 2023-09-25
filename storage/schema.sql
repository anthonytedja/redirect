CREATE TABLE IF NOT EXISTS urls (
	url_base varchar(255) PRIMARY KEY,
	url_short varchar(255) NOT NULL UNIQUE
);
