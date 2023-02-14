(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api]))

(defn wrap-handler [config]
  (update config :handler dm/wrap-handler))

(defn prepare-dev-server []
    (-> (core/load-config "config.edn")
        (core/routes (api/routes-data))
        (core/add-handler)
        (wrap-handler)
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
