(ns ouch-it-hurts.web.middlewares.core
  (:require
   [ouch-it-hurts.web.middlewares.exceptions :refer [exceptions-handler-wrapper]]
   [ouch-it-hurts.web.middlewares.format :refer :all]
   [clojure.tools.logging :as log]))

(defn log-request-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (log/infof "\n>>> %s\n-------\n<<< %s" req resp)
      resp)))

(defn wrap-handler [handler]
  (-> handler
      (format-response-body)
      (format-query-string)
      (format-request-body)
      (exceptions-handler-wrapper)))

(defn wrap-handler-with-logging [handler]
  (log-request-response handler))

