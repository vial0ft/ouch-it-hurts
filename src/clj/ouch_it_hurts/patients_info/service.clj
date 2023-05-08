(ns ouch-it-hurts.patients-info.service
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.db.core :refer [ds]]
   [ouch-it-hurts.patients-info.repo :as repo]
   [clojure.set :refer :all]
   [clojure.data :refer [diff]]))

(def ^:private default-paging {:page-size 100
                               :page-number 1})
(def ^:private default-sorting {:id :asc})

(defn- show-records-filter [{:keys [show-records-opts] :as filters}]
  (println show-records-opts)
  (-> (case show-records-opts
        :all (dissoc filters :deleted)
        :deleted-only (assoc filters :deleted true)
        (assoc filters :deleted false))
      (dissoc :show-records-opts)))

(defn- sex-opts [{:keys [sex-opts] :as filters}]
  (if sex-opts
    (-> filters
        (assoc :sex (let [coll-sex-opts (if (coll? sex-opts) (set sex-opts) #{sex-opts})]
                      (if (contains? coll-sex-opts "unknown")
                        (-> coll-sex-opts (disj "unknown") (conj nil))
                        coll-sex-opts)))
        (dissoc :sex-opts))
    filters))

(defn- birth-date-period [{:keys [birth-date-period] :as filters}]
  (if birth-date-period
    (-> filters
        (dissoc :birth-date-period)
        (assoc :birth-date birth-date-period))
    filters))

(defn- paging-2-offset [pn ps] (* (dec pn) ps))

(defn get-all
  "Fetch all patient infos according `paging`, `filters`, `sorting`"
  [get-all-req]
  (log/infof "%s" (str get-all-req))
  (let [{:keys [paging filters sorting] :or {paging default-paging sorting default-sorting}} get-all-req
        {:keys [page-number page-size]} paging
        prepered-filter (-> filters (show-records-filter) (sex-opts) (birth-date-period))]
    (repo/query-infos
     ds
     {:offset (paging-2-offset page-number page-size)
      :limit page-size
      :filters prepered-filter
      :sorting sorting})))

(defn- error-when [cond msg details]
  (if cond (throw (ex-info msg details))))

(defn get-by-id
  "Fetch patient's info by `id` long. Returns `nil` if patient's info isn't exist"
  [id]
  (repo/get-info-by-id ds id))

(defn add-patient-info
  "Add information about new patient. There cannot be patient with already existed `oms`."
  [patient-info]
  (when-some [oms (:oms patient-info)]
    (error-when
     (not (nil? (repo/get-info-by-oms ds oms)))
     "Can't add the patient's info: the patient with oms that already exists"
     {:reason :already-exist
      :oms oms}))
  (repo/insert-info ds patient-info))

(defn delete-patient-info
  "Mark patient's info record with `id` as deleted. Throw error if record not found."
  [id]
  (let [count-deleted (repo/delete-info ds id)]
    (error-when
     (zero? count-deleted)
     "Can't delete the patient's info: the patient isn't exist or already deleted"
     {:reason :not-exist-or-already-deleted :id id})
    {:id id}))

(defn- update-vals-with [m f]
  (->> m
       (map (fn [kv] [(first kv) (f kv)]))
       (into {})))

(defn update-patient-info
  "Merge current patient's info with new patient's info."
  [id patient-info]
  (println "update id = " id "info =" patient-info)
  (error-when  (nil? id) "Can't update patient info without id" {:reason :not-exists :id id})
  (let [current-patient-info (repo/get-info-by-id ds id)]
    (error-when (nil? current-patient-info) "Can't update patient's info: the patient isn't exist"
                {:reason :not-exist :id id})
    (let [[extra upd _] (diff current-patient-info patient-info)
          update (update-vals-with (merge extra upd) #(get upd (first %)))
          _ (println "for update " update)]
      (repo/update-info ds id update))))
