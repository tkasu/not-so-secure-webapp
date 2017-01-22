CREATE TABLE users (
 id VARCHAR(20) PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIME,
 is_active BOOLEAN,
 pass VARCHAR(300));

CREATE TABLE price (
code VARCHAR(255),
price VARCHAR(255),
PRIMARY KEY (code, price)
);

CREATE TABLE winner (
winner_id INT AUTO_INCREMENT PRIMARY KEY,
email VARCHAR(255) NOT NULL,
address VARCHAR(255) NOT NULL,
code VARCHAR(255) NOT NULL,
price VARCHAR(255) NOT NULL,
FOREIGN KEY (code, price) REFERENCES price (code, price)
);
