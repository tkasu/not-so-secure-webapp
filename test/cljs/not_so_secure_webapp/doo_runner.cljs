(ns not-so-secure-webapp.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [not-so-secure-webapp.core-test]))

(doo-tests 'not-so-secure-webapp.core-test)

