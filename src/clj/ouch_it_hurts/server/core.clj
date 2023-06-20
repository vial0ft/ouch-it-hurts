(ns ouch-it-hurts.server.core
  (:require [ring.adapter.jetty :as jetty]))

(defonce server (atom nil))

(defn start-server [{:keys [handler port] :as config}]
  (println (format "Server started with port %s" (Integer/parseInt port)))
  (reset! server (jetty/run-jetty handler {:port (Integer/parseInt port)
                                           :join? false
                                           :async? true})))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (.stop @server)
    (reset! server nil)
    (println "Server stopped")))
