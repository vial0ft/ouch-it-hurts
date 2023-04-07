(ns middlewares.dev-middlewares
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.web.middlewares.core :as middlewares]
   [ouch-it-hurts.web.middlewares.cors :as cors]))


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


(defn wrap-handler-dev [handler]
  (-> handler
      (cors/cors)
      (middlewares/wrap-handler)
      (middlewares/wrap-handler-with-logging)
      ))








