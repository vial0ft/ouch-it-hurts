(ns dev
  (:require
   [ouch-it-hurts.core :as core]))



(defn prepare-dev-server []
    (-> (core/load-config "config.edn")
        (core/add-handler core/app)
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
