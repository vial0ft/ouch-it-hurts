(ns ouch-it-hurts.core
  (:require
   [clojure.java.io :as io]
   [org.httpkit.server :refer [run-server]]
   [ouch-it-hurts.config-reader :as c]
   [ouch-it-hurts.web.middlewares.core :as mid]
   [ouch-it-hurts.web.routes.core :as r]
   [ouch-it-hurts.web.routes.api :as api])
  (:gen-class))


(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})

(defonce server (atom nil))


(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn load-config [config-file]
  (-> (c/read-config config-file)
      (c/resolve-props)
      (get-in [:server/http])
      (select-keys [:port])))


(defn routes [config routes-data]
  (assoc config :routes routes-data))

(defn add-handler [{:keys [routes] :as config}]
  (assoc config :handler (r/routing routes)))


(defn wrap-handler [config]
  (update config :handler mid/wrap-handler))

(defn start-server [{:keys [handler port routes]} server]
  (println (format "Server started with port %s" port))
    (reset! server (run-server handler {:port port})))

(defn run [& args]
  (-> (load-config "system.edn")
      (routes api/routes-data)
      (add-handler)
      (wrap-handler)
      (start-server server))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
  )


(defn -main [& args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and https://http-kit.github.io/migration.html#reload
  (run args))

(comment

  (-> (load-config "system.edn")
      (routes (api/routes-data))
      (add-handler)
      (wrap-handler)
      (start-server server))

  (stop-server)
  )
