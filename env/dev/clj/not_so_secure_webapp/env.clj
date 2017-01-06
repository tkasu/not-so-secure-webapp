(ns not-so-secure-webapp.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [not-so-secure-webapp.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[not-so-secure-webapp started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[not-so-secure-webapp has shut down successfully]=-"))
   :middleware wrap-dev})
