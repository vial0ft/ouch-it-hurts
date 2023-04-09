(ns ouch-it-hurts.api
  (:require [ajax.core :refer [GET POST PUT DELETE]]
            [clojure.string :as s]
            [goog.string :as gstr]))


(defn just-log [r]
  (println r))


(defn fetch-patients-info [request-params]
  (let [params (select-keys request-params [:filters :sorting :page-size :page-number])]
    (new js/Promise
         (fn [resolve reject]
           (GET "http://localhost:3000/patients"
                {:params params
                 :format :json
                 :response-format :json
                 :keywords? true
                 :handler resolve
                 :error-handler reject})))
    ))


(defn get-patient-info-by-id [id]
  (new js/Promise
       (fn [resolve reject]
         (GET  (gstr/format "http://localhost:3000/patient/%d" id)
               {:format :json
                :response-format :json
                :keywords? true
                :handler resolve
                :error-handler reject}))
       ))

(defn add-patient-info [patient-info]
  (new js/Promise
       (fn [resolve reject]
         (POST "http://localhost:3000/patients"
               {:params patient-info
                :format :json
                :keywords? true
                :handler resolve
                :error-handler reject}))
       ))

(defn update-patient-info [{:keys [id] :as patient-info}]
  (new js/Promise
       (fn [resolve reject]
         (PUT (gstr/format "http://localhost:3000/patient/%d" id)
              {:params patient-info
               :format :json
               :response-format :json
               :keywords? true
               :handler resolve
               :error-handler reject}))
       ))

(defn delete-patient-info [id]
  (new js/Promise
       (fn [resolve reject]
         (DELETE (gstr/format "http://localhost:3000/patient/%d" id)
          {:format :json
           :response-format :json
           :keywords? true
           :handler resolve
           :error-handler reject}))
       ))
