(ns ouch-it-hurts.db.core
  (:require [hikari-cp.core :refer :all]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defonce ds (atom nil))

(defn init-db-conn [db-config]
  (reset! ds (connection/->pool HikariDataSource db-config)))


(defn close-db-conn []
  (when-not (nil? @ds)
    (@ds :timeout 100)
    (close-datasource @ds)
    (reset! ds nil)
    (println "Database disconnected")))

