(ns ouch-it-hurts.patients-info.repo
  (:require
   [ouch-it-hurts.query-builder.utils :as qb-utils]
   [next.jdbc :as jdbc]
   [next.jdbc.date-time]
   [honey.sql :as sql]))

(defn- patient-info-mapper [info]
  (if (:birth-date info)
    (update info :birth-date qb-utils/sql-date->local-date)
    info))

(defn get-info-by-id [ds id]
  (let [query (-> {:select [:*] :from [:patients/info] :where [:= :id id]} sql/format)
        result (jdbc/execute-one! @ds query jdbc/unqualified-snake-kebab-opts)]
    (when result (patient-info-mapper result))))

(defn get-info-by-oms [ds oms]
  (let [query (-> {:select [:*] :from [:patients/info] :where [:= :oms oms]} sql/format)
        result (jdbc/execute-one! @ds query jdbc/unqualified-snake-kebab-opts)]
    (when result (patient-info-mapper result))))

(defn query-infos [ds {:keys [offset limit filters sorting]}]
  (let [condition (qb-utils/map->where :and filters)
        order-by (qb-utils/map->order-by sorting)
        query     (-> {:select [:*]
                       :from [:patients/info]
                       :where condition
                       :order-by order-by
                       :offset offset
                       :limit limit}
                      sql/format)
        total-query (-> {:select [:%count.*]
                         :from [:patients/info]
                         :where [condition]}
                        sql/format)]
    (jdbc/with-transaction [tx @ds]
      (let [result (jdbc/execute! tx query jdbc/unqualified-snake-kebab-opts)
            [{:keys [count]}] (jdbc/execute! tx total-query)]
        {:data (when result (map patient-info-mapper result))
         :total count}))))

(defn insert-info [ds new-patient-info]
  (let [query (-> {:insert-into [:patients/info]
                   :values [(-> new-patient-info
                                (select-keys
                                 [:first-name
                                  :last-name
                                  :middle-name
                                  :birth-date
                                  :sex
                                  :address
                                  :oms]))]
                   :returning :*}
                  (sql/format))]
    (patient-info-mapper (jdbc/execute-one! @ds query jdbc/unqualified-snake-kebab-opts))))

(defn set-deleted [ds id deleted?]
  (let [query (-> {:update [:patients/info]
                   :set {:deleted deleted?
                         :updated-at (.toInstant (java.time.OffsetDateTime/now))}
                   :where [:and [:= :id id] [:= :deleted (not deleted?)]]}
                  (sql/format))]
  (->  (jdbc/execute-one! @ds query jdbc/unqualified-snake-kebab-opts)
       (:next.jdbc/update-count))))

(defn update-info [ds id patient-info-for-update]
  (let [query (-> {:update :patients/info
                   :set (-> (select-keys patient-info-for-update
                                         [:first-name
                                          :last-name
                                          :middle-name
                                          :birth-date
                                          :sex
                                          :address
                                          :updated-at
                                          :oms])
                            (assoc :updated-at (.toInstant (java.time.OffsetDateTime/now))))
                   :where [:= :id id]}
                  sql/format)]
    (->  (jdbc/execute-one! @ds query jdbc/unqualified-snake-kebab-opts)
         (:next.jdbc/update-count))))
