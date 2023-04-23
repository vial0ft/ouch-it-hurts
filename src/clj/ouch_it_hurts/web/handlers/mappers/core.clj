(ns ouch-it-hurts.web.handlers.mappers.core
  (:require [ouch-it-hurts.specs :as specs])
  )

(def date-formatter (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(defn- patient-birth-date-de [{:keys [birth-date] :as info}]
  (if birth-date
    (->> birth-date
        (java.time.LocalDate/parse)
        (assoc info :birth-date))
    info
    ))


(defn- patient-birth-date-ser [{:keys [birth-date] :as info}]
  (if birth-date
    (->> (.format birth-date date-formatter)
         (assoc info :birth-date))
    info
    ))

(defn patient-info-deserializer [info]
  (-> info
      (patient-birth-date-de)
      ))


(defn patient-info-serializer [info]
  (-> info
      (patient-birth-date-ser)
      ))
