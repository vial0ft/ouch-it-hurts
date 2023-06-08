(ns ouch-it-hurts.patients-info.repo
  (:require
   [ouch-it-hurts.query-builder.core :as qb]
   [ouch-it-hurts.query-builder.ops :as ops]
   [ouch-it-hurts.query-builder.utils :as qb-utils]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]
   [next.jdbc.prepare :as p]
   [next.jdbc.date-time]))

(defn- patient-info-mapper [info]
  (if (:birth-date info)
    (update info :birth-date qb-utils/sql-date->local-date)
    info))

(defn get-info-by-id [ds id]
  (let [result (sql/get-by-id @ds :patients.info id jdbc/unqualified-snake-kebab-opts)]
    (when result (patient-info-mapper result))))

(defn get-info-by-oms [ds oms]
  (let [result (first (sql/find-by-keys @ds :patients.info {:oms oms} jdbc/unqualified-snake-kebab-opts))]
    (when result (patient-info-mapper result))))

(defn query-infos [ds {:keys [offset limit filters sorting]}]
  (let [condition (qb-utils/map->where ops/_and  filters qb-utils/as-snake-name)
        [query & args]       (-> (qb/select [:*])
                                 (qb/from [:patients.info])
                                 (qb/where [condition])
                                 (qb/order-by [(qb-utils/map->order-by sorting qb-utils/as-snake-name)])
                                 (qb/offset offset)
                                 (qb/limit limit)
                                 (qb/build))
        [total-query & total-args] (-> (qb/select [(ops/_count)])
                                       (qb/from [:patients.info])
                                       (qb/where [condition])
                                       (qb/build))]
    (jdbc/with-transaction [tx @ds]
      (let [ps (jdbc/prepare tx [query])
            result (jdbc/execute! (p/set-parameters ps args) nil {:builder-fn rs/as-unqualified-kebab-maps})
            total-ps (jdbc/prepare tx [total-query])
            [{:keys [count]}] (jdbc/execute! (p/set-parameters total-ps total-args))]
        {:data (when result (map patient-info-mapper result))
         :total count}))))

(defn insert-info [ds new-patient-info]
  (-> (sql/insert! @ds :patients.info
                   (-> new-patient-info
                       (select-keys
                        [:first-name
                         :last-name
                         :middle-name
                         :birth-date
                         :sex
                         :address
                         :oms]))
                   jdbc/unqualified-snake-kebab-opts)
      (patient-info-mapper)))

(defn set-deleted [ds id deleted?]
  (-> (sql/update! @ds :patients.info
                   {:deleted deleted?
                    :updated-at (.toInstant (java.time.OffsetDateTime/now))}
                   {:id id :deleted (not deleted?)} jdbc/unqualified-snake-kebab-opts)
      (:next.jdbc/update-count)))

(defn update-info [ds id patient-info-for-update]
  (let [info-for-update (-> (select-keys patient-info-for-update
                                         [:first-name
                                          :last-name
                                          :middle-name
                                          :birth-date
                                          :sex
                                          :address
                                          :updated-at
                                          :oms])
                            (assoc :updated-at (.toInstant (java.time.OffsetDateTime/now))))]
    (-> (sql/update! @ds :patients.info info-for-update {:id id :deleted false} jdbc/unqualified-snake-kebab-opts)
        (:next.jdbc/update-count))))
