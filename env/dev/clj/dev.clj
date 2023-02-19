(ns dev
  (:require
   [ouch-it-hurts.core :as core]
   [middlewares.dev-middlewares :as dm]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api]
   [ouch-it-hurts.db.core :as d]
   [next.jdbc :as jdbc]
   [ouch-it-hurts.server.core :as serv]
   [ouch-it-hurts.config-reader.core :as c]
   ))



(defn get-port [config]
  (get-in config [:server/http :port]))

(defn prepare-dev-server-config []
    (let [config  (c/load-config "config.edn")]
      (-> config
          (assoc
           :handler (-> (r/routing api/routes-data)
                        (dm/wrap-handler))
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

  (.toLocalDateTime (:t (first (jdbc/execute! @d/ds ["select now() as t"]))))
    (into [] (jdbc/execute! ds ["select 'qwe' as qwe , a from (select 1 as a ) asq"]))

 )
