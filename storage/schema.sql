CREATE TABLE IF NOT EXISTS urls (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	short_code varchar(255) NOT NULL UNIQUE,
	url_original varchar(255) NOT NULL UNIQUE
);

CREATE INDEX idx_short_code ON urls (short_code);

-- SELECT COUNT(*) FROM urls WHERE short_code = 'some_code';
-- ^ Use the above to check if a short code already exists in the database
-- if its 0, its free, if its 1 its taken, if its > 1, error out
