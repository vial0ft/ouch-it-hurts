(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]))



(defn prepare-dev-server []
    (-> (core/load-config "config.edn")
        (core/add-handler (-> core/app
                              (dm/log-request-response)
                              ))
        ))


(defn start []
  (-> (prepare-dev-server)
      (core/start-server core/server)
      ))

(defn stop []
  (core/stop-server))

(start)

(comment
  (deref core/server)
  (start)
  (stop)
 )
