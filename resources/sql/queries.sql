-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- :name delete-user! :! :n
-- :doc delete a user given the id
DELETE FROM users
WHERE id = :id

-- :name get-winners :? :*
-- :doc get all winners, no parameters
SELECT *
FROM winner

-- :name insert-winner! :! :n
-- :doc insert winner
INSERT INTO winner (email, address, code, price)
VALUES (:email, :address, :code, :price)

-- :name get-admin :? :1
-- :doc returns row if given id and password matches admin
SELECT *
FROM users
WHERE id = :id AND pass = :password AND admin = TRUE


-- :name get-avail-prices :? :*
-- :doc return all prices that are not acquired yet
SELECT *
FROM price
WHERE code NOT IN (SELECT DISTINCT code FROM winner)

-- :name delete-price! :! :n
-- :doc delete price based on given code and price
DELETE FROM price
WHERE code = :code AND price = :price

-- :name insert-price! :! :n
-- :dov insert price with given code and price
INSERT INTO price (code, price)
VALUES (:code, :price)
