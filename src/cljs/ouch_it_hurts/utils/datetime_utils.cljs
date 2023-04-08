(ns ouch-it-hurts.utils.datetime-utils
  (:require [clojure.string :as str]))



(defn now [] (new js/Date))

(defn add-days [date number-of-days]
  (let [new-date (new js/Date date)]
    (do
     (.setDate new-date (-> new-date (.getDate) (+ number-of-days)))
      new-date)))

(defn start-of-date
  ([] (start-of-date (now)))
  ([date]
   (do
     (.setHours date 0 0 0 0)
     date)))

(defn offset-date
  ([] (offset-date (now)))
  ([date] (let [offset (.getTimezoneOffset date)]
              (new js/Date (- (.getTime date) (* offset 60 1000))))))

(defn to-date [date-time]
  (-> date-time
      (.toISOString)
      (.split "T")
      (first)))


(defn parse-date [date-str]
  (new js/Date date-str))

(comment

  (let [date (parse-date "2023-02-23T21:00:00Z")]
    
    )

  )
