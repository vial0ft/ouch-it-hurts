(ns ouch-it-hurts.web.middlewares.core
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.web.middlewares.exceptions :refer [exceptions-handler-wrapper]]
   [ouch-it-hurts.web.middlewares.format :refer :all]))


(defn log-request-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (log/infof "\n>>> %s\n-------\n<<< %s" req resp)
      resp)))

(defn cors [handler]
  (fn [req]
    (let [resp (handler req)]
      (-> resp
          (assoc-in [:headers "Access-Control-Allow-Origin"]  "*")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,PUT,POST,DELETE,OPTIONS")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "X-Requested-With,Content-Type,Cache-Control")))))

(defn wrap-handler [handler]
  (-> handler
      (exceptions-handler-wrapper)
      (format-response-body)
      (format-query-string)
      (format-request-body)
      ))

(defn wrap-handler-with-logging [handler]
  (log-request-response handler))

