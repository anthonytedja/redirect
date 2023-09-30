CREATE TABLE IF NOT EXISTS urls (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	short_code varchar(255) NOT NULL UNIQUE,
	url_original varchar(255) NOT NULL
);

CREATE INDEX idx_short_code ON urls (short_code);
