(ns middlewares.dev-middlewares
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.web.middlewares.format :refer :all]))


(defn log-request [handler]
  (fn [req]
    (log/debug "Request:\n" req)
    (handler req)))


(defn log-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (log/debug "Response:\n" resp)
      resp)))


(defn log-request-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (log/debugf "\n>>> %s\n-------\n<<< %s" req resp)
      resp)))


(defn wrap-handler [handler]
  (-> handler
      (log-request-response)
      (format-query-string)
      (format-request-body)
      (format-response-body)
      ))








