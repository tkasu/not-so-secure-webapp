(ns not-so-secure-webapp.routes.home
  (:require [not-so-secure-webapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [not-so-secure-webapp.db.core]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8")))
  (POST "/code" []
        (response/ok {:body {:price (str (rand-int 100) " €")}})))

