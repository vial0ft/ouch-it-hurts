(ns ouch-it-hurts.web.middlewares.cors
  (:require [ouch-it-hurts.web.http-responses.core :as http-resp]))



(def cors-headers
  "Generic CORS headers"
  {"Access-Control-Allow-Origin"  "*"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,OPTIONS"})

(defn preflight?
  "Returns true if the request is a preflight request"
  [request]
  (= (request :request-method) :options))


(defn cors [handler]
  (fn [request]
    (-> (if (preflight? request) (http-resp/json-ok) (handler request))
                  (http-resp/with-headers cors-headers))
    ))
