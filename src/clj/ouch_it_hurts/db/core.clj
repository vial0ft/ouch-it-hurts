(ns ouch-it-hurts.db.core
  (:require [hikari-cp.core :refer :all]
            [next.jdbc.connection :as connection])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defonce ds (atom nil))

(defn init-db-conn [{:keys [host port dbtype dbname] :as db-config}]
  (let [cfg (-> db-config
                (assoc :jdbcUrl (format "jdbc:%s://%s:%s/%s" dbtype host port dbname)))]
    (reset! ds (connection/->pool HikariDataSource cfg))))

(defn close-db-conn []
  (when-not (nil? @ds)
    (close-datasource @ds)
    (reset! ds nil)
    (println "Database disconnected")))


