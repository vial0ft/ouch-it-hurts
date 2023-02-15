(ns ouch-it-hurts.web.middlewares.format
  (:require
   [cheshire.core :as json]))



(defn format-request-body [handler]
  (fn [req]
    (handler (if-let [body (:body req)]
               (case (get-in req [:headers "content-type"])
                 "application/json" (assoc-in req [:app/request :body] (json/decode
                                                                        (slurp body)))
                 req)
               req))
    ))

(defn format-response-body [handler]
  (fn [req]
    (let [response (handler req)]
      (case (get-in response [:headers "Content-type"])
        "application/json" (update response :body json/encode)
        response
        ))))

