(ns not-so-secure-webapp.app
  (:require [not-so-secure-webapp.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
