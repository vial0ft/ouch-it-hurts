(ns ouch-it-hurts.relocatus.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [ouch-it-hurts.relocatus.repo :as repo]
            [ouch-it-hurts.relocatus.helpers :as h]
            [clojure.tools.logging :as log]
            [next.jdbc :as jdbc]))

(defn- check-exists-migration-hash [existed-migration-hash migration-files-hash]
  (if (= existed-migration-hash migration-files-hash)
    [:ok {:valid-hash migration-files-hash}]
    [:error {:details (format "Migration has incorrect hash: expect %s actual %s" existed-migration-hash migration-files-hash)}]))

(defn- apply-migrations [ds migration-table {:keys [migrations start-hash]}]
  (loop [[[name {:keys [up down hash]}] & rest] migrations
         acc {:hash start-hash}]
    (if-not name acc
            (let [script-hash (h/hash-of-pair (:hash acc) hash)
                  result (repo/do-migration ds migration-table {:migration-name (h/migration-name-without-time name)
                                                                :hash script-hash} (slurp up))]
              (printf "Applying migration: %s result: %s\n" name result)
              (if (contains? result :error) result
                  (recur rest (assoc acc :hash script-hash)))))))

(defn- filter-existed-migrations [migration-scripts current-migrations]
  (loop [[[time-and-name {:keys [up down hash] :as up-down-hash}] & rest] migration-scripts
         acc {:migrations (sorted-map) :hash 0}]
    (if-not time-and-name acc
            (let [name (h/migration-name-without-time time-and-name)
                  [check-result {:keys [details valid-hash]}]
                  (if-some [exists-migration-hash (get current-migrations name)]
                    (check-exists-migration-hash exists-migration-hash (h/hash-of-pair (:hash acc) hash))
                    [:new-migration])]
              (when (= check-result :error) {:error :migration-validation-fail :migration name :details details})
              (recur rest (case check-result
                            :new-migration (update acc :migrations #(assoc % time-and-name up-down-hash))
                            (assoc acc :hash valid-hash)))))))

(defn init-migration-table [{:keys [db migration-db]}]
  (-> (jdbc/get-datasource db) (repo/create-migration-table migration-db)))

(defn create-migration [{:keys [migration-dir]} name]
  (let [formatted-date (-> (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmssSSS")
                           (.format (java.time.LocalDateTime/now)))]
    (h/create-files migration-dir [(str formatted-date "_" name ".up.sql")
                                   (str formatted-date "_" name ".down.sql")])))

(defn migrate [{:keys [migration-dir db migration-db] :as cfg}]
  (init-migration-table cfg)
  (let [ds (jdbc/get-datasource db)
        migration-table-name (h/schema-table migration-db)
        scripts-map (->> (h/migration-scripts-map migration-dir)
                         (reduce-kv (fn [m k v] (assoc m k (assoc v :hash (hash (slurp (:up v)))))) {}))
        current-migrations (->> (repo/get-migrations ds migration-table-name)
                                (reduce #(assoc %1 (:migration-name %2) (:hash %2)) {}))
        filtered-result (filter-existed-migrations scripts-map current-migrations)
        _ (when (contains? filtered-result :error) (throw (ex-info "Error of existing migrations" filtered-result)))
        result (apply-migrations ds migration-table-name filtered-result)]
    (when (contains? result :error) (throw ex-info "Error during applying migration" result))
    [:ok {:migrations (keys filtered-result)}]))

(defn rollback [{:keys [db migration-db migration-dir]}]
  (let [ds (jdbc/get-datasource db)
        migration-table-name (h/schema-table migration-db)
        migrations (repo/get-migrations ds migration-table-name)
        _ (when-not migrations (throw "There are no migrations for rolling back"))
        [last-migration previous-migration & _] (sort-by :id > migrations)
        {:keys [up down]} (h/up-down-migration-scripts migration-dir (:migration-name last-migration))
        checked-hash (h/hash-of-pair (get previous-migration :hash 0) (hash (slurp up)))
        _ (when-not (= (:hash last-migration) checked-hash)
            (throw (ex-info "Error during rollback"
                            {:error :incorrect-hash
                             :migration (:migration-name last-migration)
                             :details (format "Incorrect hash of last migration: expected %s actual %s"
                                              checked-hash (:hash last-migration))})))
        result (repo/do-rollback ds migration-table-name (:migration-name last-migration) (slurp down))]
    (when result [:ok {:rolledback-migration last-migration}])))

