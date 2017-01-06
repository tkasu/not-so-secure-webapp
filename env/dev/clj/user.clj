(ns user
  (:require [mount.core :as mount]
            [not-so-secure-webapp.figwheel :refer [start-fw stop-fw cljs]]
            not-so-secure-webapp.core))

(defn start []
  (mount/start-without #'not-so-secure-webapp.core/http-server
                       #'not-so-secure-webapp.core/repl-server))

(defn stop []
  (mount/stop-except #'not-so-secure-webapp.core/http-server
                     #'not-so-secure-webapp.core/repl-server))

(defn restart []
  (stop)
  (start))


