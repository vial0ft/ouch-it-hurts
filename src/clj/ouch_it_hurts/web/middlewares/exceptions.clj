(ns ouch-it-hurts.web.middlewares.exceptions
  (:require [ouch-it-hurts.web.http-responses.core :as http-responses]))




(defn exceptions-handler-wrapper [handler]
  (fn [req]
    (try
      (handler req)
      (catch Exception e
        (-> (http-responses/internal-server-error
         {
          :request (select-keys req [:headers :uri :query-string :request-method :app/request])
          :error {
                  :message (ex-message e)
                  :details (or (ex-data e) e)
                  }
          }
         )
            (http-responses/with-headers {"Content-type" "application/json"}))
        ))
    )
  )
