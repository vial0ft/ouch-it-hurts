(ns ouch-it-hurts.core
  (:require
   [clojure.java.io :as io]
   [org.httpkit.server :refer [run-server]]
   [ouch-it-hurts.config-reader.core :as c]
   [ouch-it-hurts.web.middlewares.core :as mid]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api]
   [ouch-it-hurts.db.core :as d])
  (:gen-class))

(defonce server (atom nil))


;; Config

(defn load-config [config-file]
  (-> (c/read-config config-file)
      (c/resolve-props)
      ))


(defn get-port [config]
  (get-in config [:server/http :port]))

(defn routes [config routes-data]
  (assoc config :routes routes-data))

(defn add-handler [{:keys [routes] :as config}]
  (assoc config :handler (r/routing routes)))


(defn wrap-handler [config]
  (update config :handler mid/wrap-handler))


;; Server start/stop  control

(defn start-server [{:keys [handler routes] :as config} server]
  (let [port (get-port config)]
  (println (format "Server started with port %s" port))
    (reset! server (run-server handler {:port port}))))


(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)
    (println "Server stopped")))

(defn run [& args]
  (let [db-init-f (fn [config]
                    (d/init-db-conn (:db/connection config))
                    config)]
  (-> (load-config "system.edn")
        (routes api/routes-data)
        (add-handler)
        (wrap-handler)
        (start-server server)))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
  )


(defn -main [& args]
  (run args))

(comment

  (-> (load-config "system.edn")
      (routes (api/routes-data))
      (add-handler)
      (wrap-handler)
      (start-server server))

  (stop-server server)
  )
