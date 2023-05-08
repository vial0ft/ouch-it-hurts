(ns ouch-it-hurts.api
  (:require [ajax.core :refer [GET POST PUT DELETE]]
            [clojure.string :as s]
            [goog.string :as gstr]
            [cljs.core :refer [goog-define]]))

(goog-define host "")

(goog-define port "")

(def uri (gstr/format "http://%s:%s" host port))

(defn url [path] (gstr/format "%s%s" uri path))

(defn fetch-patients-info [request-params]
  (let [params (select-keys request-params [:filters :sorting :paging])]
    (new js/Promise
         (fn [resolve reject]
           (GET (url "/patients")
             {:params params
              :format :json
              :response-format :json
              :keywords? true
              :handler resolve
              :error-handler reject})))))

(defn get-patient-info-by-id [id]
  (new js/Promise
       (fn [resolve reject]
         (GET  (url (gstr/format "/patient/%d" id))
           {:format :json
            :response-format :json
            :keywords? true
            :handler resolve
            :error-handler reject}))))

(defn add-patient-info [patient-info]
  (new js/Promise
       (fn [resolve reject]
         (POST (url "/patient")
           {:params patient-info
            :format :json
            :keywords? true
            :handler resolve
            :error-handler reject}))))

(defn update-patient-info [{:keys [id] :as patient-info}]
  (new js/Promise
       (fn [resolve reject]
         (PUT (url (gstr/format "/patient/%d" id))
           {:params patient-info
            :format :json
            :response-format :json
            :keywords? true
            :handler resolve
            :error-handler reject}))))

(defn delete-patient-info [id]
  (new js/Promise
       (fn [resolve reject]
         (DELETE (url (gstr/format "/patient/%d" id))
           {:format :json
            :response-format :json
            :keywords? true
            :handler resolve
            :error-handler reject}))))
