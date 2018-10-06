CREATE TABLE IF NOT EXISTS orders (
  id VARCHAR(64) NOT NULL PRIMARY KEY,
  store VARCHAR(64) NOT NULL,
  reference VARCHAR(64) NOT NULL,
  description VARCHAR(64),
  UNIQUE (store, reference)
);