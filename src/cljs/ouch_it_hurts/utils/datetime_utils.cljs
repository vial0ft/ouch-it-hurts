(ns ouch-it-hurts.utils.datetime-utils
  (:require [goog.string :as gstr]))

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
  (let [y (.getFullYear date-time)
        m (inc (.getMonth date-time))
        d (.getDate date-time)]
    (gstr/format "%d-%02d-%02d" y m d)))

(defn parse-date [date-str]
  (new js/Date date-str))

