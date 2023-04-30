(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]
   [ouch-it-hurts.web.middlewares.routes-resolver :as r]
   [ouch-it-hurts.web.routes.api :as api]
   [ouch-it-hurts.web.routes.pages :as pages]
   [ouch-it-hurts.web.middlewares.assets-resolver :refer :all]
   [ouch-it-hurts.db.core :as d]
   [ouch-it-hurts.server.core :as serv]
   [ouch-it-hurts.config-reader.core :as c]
   ))



(defn get-port [config]
  (get-in config [:server/http :port]))

(defn prepare-dev-server-config []
    (let [config  (c/load-config "config.edn")]
      (-> config
          (assoc
           :handler (-> (r/routing (api/routes-data nil) (pages/pages))
                        (dm/wrap-handler-dev)
                        (assets-resolver-wrapper (:application/assets config))
                        )
           :port (get-port config)
           ))
      ))

(defn- db-init-f [config]
  (d/init-db-conn (:db/connection config))
  config)

(defn start []
    (-> (prepare-dev-server-config)
        (db-init-f)
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
