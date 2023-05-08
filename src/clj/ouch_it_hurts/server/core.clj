(ns ouch-it-hurts.server.core
  (:require [org.httpkit.server :refer [run-server]]))


(defonce server (atom nil))


(defn start-server [{:keys [handler port] :as config}]
  (println (format "Server started with port %s" (Integer/parseInt port)))
  (reset! server (run-server handler {:port (Integer/parseInt port)})))


(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)
    (println "Server stopped")))
