(ns ouch-it-hurts.patients-info.repo
  (:require
   [ouch-it-hurts.db.core :refer [ds]]
   [ouch-it-hurts.query-builder.core :as qb]
   [ouch-it-hurts.query-builder.ops :as ops]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [next.jdbc.sql :as sql]
   [clojure.string :as str]
   [next.jdbc.types :refer [as-other]]))


(defn get-info-by-id [id]
  (sql/get-by-id @ds
                 :patients.info
                 id
                 jdbc/unqualified-snake-kebab-opts))


(defn get-info-by-oms [oms]
  (first (sql/find-by-keys @ds :patients.info {:oms oms} jdbc/unqualified-snake-kebab-opts)))

(defn- map->where [filters]
  (apply ops/_and (map (fn [[k v]] (ops/eq k v)) filters)))

(defn- map->order-by [orders]
  (let [res (flatten (vec orders))] (if-not (empty? res) res nil)))

(defn query-infos [{:keys [offset limit filters sorting]}]
  (let [condition (map->where filters)
        query       (-> (qb/select :*)
                        (qb/from :patients.info)
                        (qb/where condition))
        total-query (-> (qb/count :*)
                        (qb/from :patients.info)
                        (qb/where condition))]
    (jdbc/with-transaction [tx @ds]
      (let [result (sql/query @ds [(-> query
                                       (qb/order-by (map->order-by sorting))
                                       (qb/offset offset)
                                       (qb/limit limit))] {:builder-fn rs/as-unqualified-kebab-maps})
            [{:keys [count]}] (sql/query @ds [total-query])]
        (println "total " count)
        {:data result
         :total count}))))

(defn insert-info [new-patient-info]
  (sql/insert!
   @ds
   :patients.info
   (-> new-patient-info
       (update :sex as-other)
       (select-keys
        [:first-name
         :second-name
         :middle-name
         :birth-date
         :sex
         :address
         :oms]))
   jdbc/unqualified-snake-kebab-opts))

(defn delete-info [id]
  (-> (sql/update! @ds
                   :patients.info
                   {:deleted true}
                   {:id id :deleted false}
                   jdbc/unqualified-snake-kebab-opts)
      (:next.jdbc/update-count)))



(defn update-info [patient-info-for-update]
  (-> (sql/update! @ds
                   :patients.info
                   (-> patient-info-for-update
                       (update :sex as-other)
                       (select-keys
                        [:first-name
                         :second-name
                         :middle-name
                         :birth-date
                         :sex
                         :address
                         :oms
                         :update-at]))
                   {:id (:id patient-info-for-update) :deleted false}
                   jdbc/unqualified-snake-kebab-opts)
      (:next.jdbc/update-count)))

