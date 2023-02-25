(ns ouch-it-hurts.web.handlers.patient-info
  (:require [ouch-it-hurts.patients-info.service :as s]))






(defn as-json-response [handler]
  (fn [req]
    {:status 200
     :headers {"Content-type" "application/json"}
     :body   (handler req)}
    ))

(defn get-all [req]
  (as-json-response )
  )

(defn get-by-id [req]
  {:status  200
   :headers {"Content-type" "application/json"}
   :body   (get-in req [:app/request :path-params :id])}
  )

(defn routes []
  [
   ["/patients" {:get {:handler (as-json-response s/get-all)}
                 :post {:handler (as-json-response s/add-patient-info)}}]
   ["/patient/:id" {:get {:handler (as-json-response s/get-by-id)}
                    :put {:handler (as-json-response s/update-patient-info)}
                    :delete {:handler (as-json-response s/delete-patient-info)}}]
   ])
