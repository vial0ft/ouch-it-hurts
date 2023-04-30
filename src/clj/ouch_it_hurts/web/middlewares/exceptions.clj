(ns ouch-it-hurts.web.middlewares.exceptions
  (:require [ouch-it-hurts.web.http-responses.core :as http-responses]
            [clojure.tools.logging :as log]))




(defn exceptions-handler-wrapper [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (log/error e)
        (-> (http-responses/internal-server-error
             {:error {:message (ex-message e)
                      :details (or (ex-data e) e)}})
            (http-responses/response-as-json))))))
