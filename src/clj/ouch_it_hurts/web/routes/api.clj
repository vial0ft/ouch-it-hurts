(ns ouch-it-hurts.web.routes.api
  (:require
   [ouch-it-hurts.web.routes.core :as core]
   [ouch-it-hurts.web.handlers.patient-info :as p]
   [ouch-it-hurts.web.handlers.health :as h]))


(defn routes-data []
  (reduce into [] [(h/routes) (p/routes)]))
