(ns not-so-secure-webapp.db.core
  (:require
    [conman.core :as conman]
    [mount.core :refer [defstate]]
    [not-so-secure-webapp.config :refer [env]]
    [clojure.java.jdbc :as jdbc]))

(defstate ^:dynamic *db*
           :start (conman/connect! {:jdbc-url (env :database-url)})
           :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")

(defn get-prices [code] 
  (jdbc/query *db* [(str 
                     "select * from price where code = '" code "' and code not in (select code from winner)")]))

#_(defn dev-free-query [q]
  (jdbc/query *db* [(str q)]))


