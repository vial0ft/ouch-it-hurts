(ns ouch-it-hurts.patients-info.service
  (:require
   [clojure.tools.logging :as log]
   [ouch-it-hurts.patients-info.repo :as repo]
   [clojure.set :refer :all]))


(def ^:private default-offset "0")
(def ^:private default-limit "100")
(def ^:private default-sorting {:id :asc})


(defn- filter-map [m pred f]
  (transduce
   (comp
    (filter pred)
    (map f)
    )
   into {}
   m))

(comment
  )

(defn get-all
  "Fetch all patient infos according `offset`, `limit`, `filters`, `sorting`"
  [get-all-req]
  (log/infof "%s" (str get-all-req))
  (let [{:keys [offset limit filters sorting]
         :or {offset default-offset limit default-limit sorting default-sorting}} get-all-req
        result (repo/query-infos {
                                  :offset offset
                                  :limit limit
                                  :filters filters
                                  :sorting (update-vals sorting keyword)
                                  })
        _ (log/debug result)]
    result))


(defn- error-when [cond msg details]
  (if cond
    (throw (ex-info msg details))))

(defn get-by-id
  "Fetch patient's info by `id` long. Returns `nil` if patient's info isn't exist"
  [id]
  (repo/get-info-by-id id))

(defn add-patient-info
  "Add information about new patient. There cannot be patient with already existed `oms`."
  [patient-info]
  (when-some [oms (:oms patient-info)]
    (error-when
     (not (nil? (repo/get-info-by-oms oms)))
     "Can't add the patient's info: the patient with oms that already exists"
     {:reason :already-exist
      :oms oms}))
  (repo/insert-info patient-info))


(defn delete-patient-info
  "Mark patient's info record with `id` as deleted. Throw error if record not found."
  [id]
  (let [count-deleted (repo/delete-info id)]
    (error-when
     (zero? count-deleted)
     "Can't delete the patient's info: the patient isn't exist or already deleted"
     {:reason :not-exist-or-already-deleted
      :id id})
    {:id id}
    ))



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
  [id patient-info]
  (error-when
   (nil? id)
   "Can't update patient info without id"
     {:reason :not-exists
      :id id})
  (let [current-patient-info (repo/get-info-by-id id)]
    (error-when
     (nil? current-patient-info)
     "Can't update patient's info: the patient isn't exist"
     {:reason :not-exist
      :id id})
    (repo/update-info (assoc (merge-info current-patient-info patient-info) :update-at (java.time.OffsetDateTime/now)))
    ))

