(ns ouch-it-hurts.web.routes.api
  (:require [ouch-it-hurts.web.routes.core :as core]))



(defn hello [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})


(defn echo [req]
  {:status  200
   :headers {"Content-Type" "text/plain"}
   :body    req})



(defn routes-data []
  [
   ["/"
    {:get {:handler app}}
    ]
   ["/echo"
    {:get {:handler echo}}]
   ])


