(ns ouch-it-hurts.core
  (:require
   [clojure.java.io :as io]
   [org.httpkit.server :refer [run-server]]
   [ouch-it-hurts.config-reader :as c])
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

(defn add-handler [config handler]
  (assoc config :handler handler))

(defn start-server [{:keys [handler port]} server]
  (println (format "Server started with port %s" port))
    (reset! server (run-server handler {:port port})))

(defn run [& args]
  (-> (load-config "system.edn")
      (add-handler app)
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
      (add-handler app)
      (start-server server))

  (stop-server)
  )
