(ns ouch-it-hurts.web.middlewares.core
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.web.middlewares.format :refer :all]))


(defn log-request-response [handler]
  (fn [req]
    (let [resp (handler req)]
      (log/infof "\n>>> %s\n-------\n<<< %s" req resp)
      resp)))

(defn wrap-handler [handler]
  (-> handler
      (log-request-response)
      (format-query-string)
      (format-request-body)
      (format-response-body)
      ))

