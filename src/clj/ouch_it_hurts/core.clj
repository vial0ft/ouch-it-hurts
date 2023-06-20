(ns ouch-it-hurts.core
  (:require
   [ouch-it-hurts.config-reader.core :as c]
   [ouch-it-hurts.web.routes.api :refer [routes-data]]
   [ouch-it-hurts.web.middlewares.core :as mid]
   [ouch-it-hurts.db.core :as d]
   [ouch-it-hurts.server.core :as serv]
   [ouch-it-hurts.relocatus.core :as relocat])
  (:gen-class))

(defn get-port [config]
  (get-in config [:server/http :port]))

(defn prepare-config []
  (let [config  (c/load-config "system.edn")]
    (-> config
        (assoc
         :handler (mid/wrap-handler (routes-data nil) config)
         :port (get-port config)))))

(defn- relocatus-migrations [config]
  (let [relocat-config (:relocatus/migrations config)]
    (relocat/init-migration-table relocat-config)
    (relocat/migrate relocat-config)
    config))

(defn- db-init-f [config]
  (d/init-db-conn (:db/connection config))
  config)

(defn stop-server []
  (d/close-db-conn)
  (serv/stop-server))

(defn run [& args]
  (-> (prepare-config)
      (db-init-f)
      (relocatus-migrations)
      (serv/start-server)))
(.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))

(defn -main [& args]
  (println "Run Application")
  (run args))

(comment

  (-> (prepare-config)
      (serv/start-server))

  (stop-server))
