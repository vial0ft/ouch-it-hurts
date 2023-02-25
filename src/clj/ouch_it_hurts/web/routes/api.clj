(ns ouch-it-hurts.web.routes.api
  (:require
   [ouch-it-hurts.web.routes.core :as core]
   [ouch-it-hurts.web.handlers.patient-info :as p]))



(defn hello [req]
  {:status  200
   :headers {"Content-type" "text/html"}
   :body    "hello HTTP!"})


(defn echo [req]
  (println "Request params: " (get req :app/request))
  (println (get req :app/request))
  {:status  200
   :headers {"Content-type" "application/json"}
   :body   (get req :app/request)})



(defn routes-data []
  (reduce into [] [(p/routes)]))

(comment

  (reduce into [] [(routes-data) (p/routes)])

  (routes-data)

  )
