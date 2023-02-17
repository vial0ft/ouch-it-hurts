(ns ouch-it-hurts.web.routes.core
  (:require [ouch-it-hurts.routing.core :as r]))


(def bad-request {:status 400})

(defn routing [routes]
  (fn [req]
    (let [[path-params handler :as routing-result] (r/get-path-params-and-handler req routes)]
      (if (empty? routing-result) bad-request)
      (handler (if-not (empty? path-params)
                 (assoc-in req [:app/request :path-params] path-params)
                 req)))
    ))
