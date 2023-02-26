(ns ouch-it-hurts.core
  (:require
   [clojure.java.io :as io]
   [ouch-it-hurts.config-reader.core :as c]
   [ouch-it-hurts.web.middlewares.core :as mid]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api]
   [ouch-it-hurts.db.core :as d]
   [ouch-it-hurts.web.routes.core]
   [ouch-it-hurts.server.core :as serv])
  (:gen-class))

(defn get-port [config]
  (get-in config [:server/http :port]))

(defn prepare-config[]
  (let [config  (c/load-config "system.edn")]
    (-> config
        (assoc
         :handler (-> (r/routing (api/routes-data))
                      (mid/wrap-handler-with-logging))
         :port (get-port config)
         ))
    ))


(defn stop-server []
  (d/close-db-conn)
  (serv/stop-server))


(defn run [& args]
  (let [db-init-f (fn [config] (d/init-db-conn (:db/connection config))
                    config)]
    (-> (prepare-config)
        (db-init-f)
        (serv/start-server)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server)))


(defn -main [& args]
  (run args))

(comment

  (-> (prepare-config)
      (serv/start-server))

  (stop-server)
  )
