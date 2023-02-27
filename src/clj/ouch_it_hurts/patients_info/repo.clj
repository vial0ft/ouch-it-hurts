(ns ouch-it-hurts.patients-info.repo
  (:require
   [ouch-it-hurts.db.core :refer [ds]]
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
  (first (sql/find-by-keys @ds :patients.info {:oms oms} jdbc/unqualified-snake-kebab-opts))
  )


(defn query-infos [{:keys [offset limit filters]}]
  (sql/query @ds
             ["select * from patients.info
                   limit ? offset ?" limit offset]
             {:builder-fn rs/as-unqualified-kebab-maps}))

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
                         :oms]))
                   {:id (:id patient-info-for-update) :deleted false}
                   jdbc/unqualified-snake-kebab-opts)
      (:next.jdbc/update-count)))



(comment

  (delete-info 5)
  )
