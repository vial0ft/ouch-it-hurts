(ns ouch-it-hurts.patients-info.service
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.patients-info.repo :as repo]
   [clojure.set :refer :all]))



(def ^:private default-offset 0)
(def ^:private default-limit 100)


(defn- filter-map [m pred f]
  (transduce
   (comp
    (filter pred)
    (map f)
    )
   into {}
   m))


(defn get-all
  "Fetch all patient infos according `offset`, `limit` and `filters`"
  [get-all-req]
  (let [{:keys [offset limit filters] :or {offset default-offset limit default-limit}} get-all-req]
    (repo/query-infos {
                      :offset offset
                      :limit limit
                      :filters filters
                      })))


(defn- error-when [cond error-msg]
  (if cond
    (throw (ex-info error-msg))))

(defn get-by-id
  "Fetch patient's info by `id`. Returns `nil` if patient's info isn't exist"
  [req]
  (let [id (parse-long (get-in req [:app/request :path-params :id]))]
  (repo/get-info-by-id id)))

(defn add-patient-info
  "Add information about new patient. There cannot be any `id` or already existed `oms`."
  [patient-info]
  (error-when (not (nil? (:id patient-info)))
              "Can't add new patient with defined id")
  (when-some [oms (:oms patient-info)]
    (error-when (not (nil? (repo/get-info-by-oms oms)))
                (format "Can't add the patient's info: the patient with oms %s already exists" oms)))
  (repo/insert-info patient-info))


(defn delete-patient-info
  "Mark patient's info record with `id` as deleted. Throw error if record not found."
  [req]
  (let [id (parse-long (get-in req [:app/request :path-params :id]))
        count-deleted (repo/delete-info id)]
    (error-when (zero? count-deleted)
                (format "Can't delete the patient's info: the patient with id %s not exist" id))))



(defn- merge-info [cur new]
  (let [new-keys-set (set (keys new))
        cur-keys-set (set (keys cur))
        delete-keys (difference cur-keys-set new-keys-set)
        merged (merge cur new)]
    (merge merged
           (filter-map
            merged
            #(contains? delete-keys (first %))
            (fn [[k v]] {k nil})))))

(defn update-patient-info
  "Merge current patient's info with new patient's info."
  [patient-info]
  (let [id (:id patient-info)]
    (error-when (nil? id) "Can't update patient info without id")
    (let [current-patient-info (repo/get-info-by-id id)]
      (error-when (nil? current-patient-info) (format "Can't update patient's info: the patient with id %s not exist" id))
      (repo/update-info (merge-info current-patient-info patient-info))
    )))


(comment
  )





