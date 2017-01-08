(ns not-so-secure-webapp.routes.home
  (:require [not-so-secure-webapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [not-so-secure-webapp.db.core :as db]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
       (response/header "Content-Type" "text/plain; charset=utf-8")))
  (POST "/code" []
        (fn [req] 
          (let [req-code (get-in req [:params :code])
                prices (db/get-prices req-code)] 
            (response/ok 
             {:body {:prices prices
                     :req-code req-code}})))))

