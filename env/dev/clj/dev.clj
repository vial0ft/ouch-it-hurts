(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api]
   [ouch-it-hurts.db.core :as d]
   [next.jdbc :as jdbc]
   [next.jdbc.connection :as connection]
   ))

(defn wrap-handler [config]
  (update config :handler dm/wrap-handler))

(defn prepare-dev-server-config []
    (-> (core/load-config "config.edn")
        (core/routes (api/routes-data))
        (core/add-handler)
        (wrap-handler)
        ))


(defn start []
  (let [db-init-f (fn [config]
                    (d/init-db-conn (:db/connection config))
                    config)]
    (-> (prepare-dev-server-config)
        (db-init-f)
        (core/start-server core/server)
        )))

(defn stop []
  (d/close-db-conn)
  (core/stop-server))

 (start)


(comment
  (start)
  (stop)

  (let [db-init-f (fn [config]
                    (d/init-db-conn (:db/connection config))
                    config)]
    (-> (prepare-dev-server-config)
        (db-init-f)
        ))

  (.toLocalDateTime (:t (first (jdbc/execute! @d/ds ["select now() as t"]))))
    (into [] (jdbc/execute! ds ["select 'qwe' as qwe , a from (select 1 as a ) asq"]))

 )
