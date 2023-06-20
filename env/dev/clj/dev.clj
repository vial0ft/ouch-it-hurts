(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]
   [ouch-it-hurts.db.core :as d]
   [ouch-it-hurts.server.core :as serv]
   [ouch-it-hurts.config-reader.core :as c]
   [ouch-it-hurts.relocatus.core :as relocat]
   [ouch-it-hurts.web.routes.api :as api]
   ))



(defn get-port [config]
  (get-in config [:server/http :port]))

(defn prepare-dev-server-config []
    (let [config  (c/load-config "config.edn")]
      (-> config
          (assoc
           :handler (dm/wrap-handler-dev (api/routes-data nil) config)
           :port (get-port config)
           ))
      ))

(defn- relocatus-migrations [config]
  (let [relocat-config (:relocatus/migrations config)]
    (relocat/init-migration-table relocat-config)
    (relocat/migrate relocat-config)
    config))

(defn- db-init-f [config]
  (d/init-db-conn (:db/connection config))
  config)

(defn start []
    (-> (prepare-dev-server-config)
        (db-init-f)
        (relocatus-migrations)
        (serv/start-server)
        ))

(defn stop []
  (d/close-db-conn)
  (core/stop-server))

(start)


(comment
  (start)
  (stop)

 )
