(ns ouch-it-hurts.api
  (:require [ajax.core :refer [GET POST PUT DELETE]]
            [clojure.string :as s]
            [goog.string :as gstr]))


(defn just-log [r]
  (println r))


(defn fetch-patients-info [{:keys [offset limit filters]}]
  (GET "http://localhost:3000/patients"
       {:params {
                 :offset offset
                 :limit limit}
        :format :json
        :handler just-log
        :error-handler just-log}))


(defn get-patient-info-by-id [id on-success on-error]
  (GET  (gstr/format "http://localhost:3000/patient/%d" id)
        {
         :format :json
         :handler on-success
         :error-handler on-error}))

(defn add-patient-info [patient-info on-success on-error]
  (POST "http://localhost:3000/patients"
        {:params patient-info
         :format :json
         :handler on-success
         :error-handler on-error}))

(defn update-patient-info [id patient-info on-success on-error]
  (PUT (gstr/format "http://localhost:3000/patient/%d" id)
      {:params patient-info
       :format :json
       :handler on-success
       :error-handler on-error}))

(defn delete-patient-info [id on-success on-error]
  (DELETE (gstr/format "http://localhost:3000/patient/%d" id)
          {:format :json
           :handler on-success
           :error-handler on-error}))


(comment

  (GET "http://localhost:3000/patient/5"
       {:format :json
        :handler just-log
        :error-handler just-log})
)


