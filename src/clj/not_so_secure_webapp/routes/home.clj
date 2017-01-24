(ns not-so-secure-webapp.routes.home
  (:require [not-so-secure-webapp.layout :as layout]
            [compojure.core :refer [defroutes GET POST DELETE PUT]]
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
  (GET "/winners" []
       (let [winners (db/get-winners)]
         (response/ok
          {:body {:winners winners}})))
  (GET "/prices" []
       (let [prices (db/get-avail-prices)]
         (response/ok
          {:body {:prices prices}})))
  (POST "/code" []
        (fn [req] 
          (let [req-code (get-in req [:params :code])
                prices (db/get-prices req-code)] 
            (response/ok 
             {:body {:prices prices
                     :req-code req-code}}))))
  (POST "/redeem" []
        (fn [req] 
          (let [code (get-in req [:params :code])
                price (get-in req [:params :price])
                email (get-in req [:params :email])
                address (get-in req [:params :address])] 
            (do
              (db/insert-winner! {:code code
                                  :price price
                                  :email email
                                  :address address})
              (response/ok {:body nil})))))
  (POST "/signin" []
        (fn [req] 
          (let [user-id (get-in req [:params :id])
                password (get-in req [:params :password])] 
            (if (not-empty (db/get-admin {:id user-id :password password}))
              (response/ok {:body nil})
              (response/unauthorized {:body nil})))))
  (DELETE "/price" []
          (fn [req]
            (let [code (get-in req [:params :code])
                  price (get-in req [:params :price])]
              (do
                (db/delete-price! {:code code :price price})
                (response/ok {:body nil})))))
  (PUT "/price" []
          (fn [req]
            (let [code (get-in req [:params :code])
                  price (get-in req [:params :price])]
              (do
                (db/insert-price! {:code code :price price})
                (response/ok {:body nil}))))))

