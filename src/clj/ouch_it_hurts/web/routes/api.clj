(ns ouch-it-hurts.web.routes.api
  (:require
   [reitit.ring :as ring]
   [reitit.ring.middleware.exception :as exception]
   [ring.util.response :refer [redirect]]
   [ouch-it-hurts.web.handlers.patient-info :as pi]
   [ouch-it-hurts.web.handlers.health :as h]))

(defn routes-data [opt] ;; TODO: add basic-url opt
  (ring/ring-handler
   (ring/router
    [
     ["/health" {:get {:handler h/healthcheck!}}]
     ["/patients" {:post {:handler pi/get-all}}]
     ["/patient" {:post {:handler pi/add-new}}]
     ["/patient/:id" {:get {:handler pi/get-by-id}
                      :put {:handler pi/update-info}
                      :delete {:handler pi/delete}}]
     ["/patient/:id/restore" {:post {:handler pi/restore-by-id}}]]
    {:data {:middleware [exception/exception-middleware]}})
   (ring/create-default-handler {:not-found (fn [_] (redirect "/index.html"))}))
  )
