(ns ouch-it-hurts.web.handlers.health
  (:require
   [ring.util.http-response :as http-response]
   [ring.util.response :as resp])
  (:import
   [java.util Date]))

(defn healthcheck!
  ([req]
   (http-response/ok
    {:time     (str (Date. (System/currentTimeMillis)))
     :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
     :app      {:status  "up"
                :message ""}}))
  ([req responde raise]
   (responde (http-response/ok
              {:time     (str (Date. (System/currentTimeMillis)))
               :up-since (str (Date. (.getStartTime (java.lang.management.ManagementFactory/getRuntimeMXBean))))
               :app      {:status  "up"
                          :message ""}}))))

(def routes
    [["/health" {:get {:handler healthcheck!}}]])
