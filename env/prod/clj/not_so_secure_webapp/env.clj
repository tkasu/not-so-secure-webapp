(ns not-so-secure-webapp.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[not-so-secure-webapp started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[not-so-secure-webapp has shut down successfully]=-"))
   :middleware identity})
