(ns ouch-it-hurts.web.handlers.health
  (:require
   [ouch-it-hurts.web.http-responses.core :as http-response])
  (:import
   [java.util Date]))

(defn healthcheck!
  [req]
  (http-response/ok
   {:time     (str (Date. (System/currentTimeMillis)))
    :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
    :app      {:status  "up"
               :message ""}}))

(defn routes []
  [["/health" {:get {:handler healthcheck!}}]])
