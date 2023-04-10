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

(defn- as-snake-name [value]
  (let [replacement-f #(str/replace % #"-" "_")]
    (cond
      (keyword? value) (if-let [ns (namespace value)]
                         (keyword ns (replacement-f (name value)))
                         (keyword (replacement-f (name value))))
      (string? value) (replacement-f value)
      :else value)
    ))

(defn- map->where [filters column-names-converter]
  (->> filters
      (map (fn [[k v]] (ops/eq (column-names-converter k) v)))
      (apply ops/_and)))

(defn- map->order-by [orders column-names-converter]
  (let [res   (->> orders
                   (map (fn [[k v]] [(column-names-converter k) v]))
                   (flatten))]
    (if-not (empty? res) res nil)))

(defn query-infos [{:keys [offset limit filters sorting]}]
  (let [condition (map->where filters as-snake-name)
        query       (-> (qb/select :*)
                        (qb/from :patients.info)
                        (qb/where condition)
                        (qb/order-by (map->order-by sorting as-snake-name))
                        (qb/offset offset)
                        (qb/limit limit))
        total-query (-> (qb/select-count :*)
                        (qb/from :patients.info)
                        (qb/where condition))]
    (println query)
    (jdbc/with-transaction [tx @ds]
      (let [result (sql/query tx [query] {:builder-fn rs/as-unqualified-kebab-maps})
            [{:keys [count]}] (sql/query tx [total-query])]
        (println "total " count)
        {:data result
         :total count}))))

(comment
  (query-infos {:filters {:first-name "Иван"}})

  )


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

