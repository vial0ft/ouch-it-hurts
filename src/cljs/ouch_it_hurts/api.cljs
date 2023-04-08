(ns ouch-it-hurts.api
  (:require [ajax.core :refer [GET POST PUT DELETE]]
            [clojure.string :as s]
            [goog.string :as gstr]))


(defn just-log [r]
  (println r))


(defn fetch-patients-info [request-params on-success on-error]
  (let [params (select-keys request-params [:filters :sorting :page-size :page-number])]
  (GET "http://localhost:3000/patients"
       {:params params
        :format :json
        :response-format :json
        :keywords? true
        :handler on-success
        :error-handler on-error})))


(defn get-patient-info-by-id [id on-success on-error]
  (GET  (gstr/format "http://localhost:3000/patient/%d" id)
        {
         :format :json
         :response-format :json
         :keywords? true
         :handler on-success
         :error-handler on-error}))

(defn add-patient-info [patient-info on-success on-error]
  (POST "http://localhost:3000/patients"
        {:params patient-info
         :format :json
         :keywords? true
         :handler on-success
         :error-handler on-error}))

(defn update-patient-info [patient-info on-success on-error]
  (PUT (gstr/format "http://localhost:3000/patient/%d" (:id patient-info))
      {:params patient-info
       :format :json
       :response-format :json
       :keywords? true
       :handler on-success
       :error-handler on-error}))

(defn delete-patient-info [id on-success on-error]
  (DELETE (gstr/format "http://localhost:3000/patient/%d" id)
          {:format :json
           :response-format :json
           :keywords? true
           :handler on-success
           :error-handler on-error}))

