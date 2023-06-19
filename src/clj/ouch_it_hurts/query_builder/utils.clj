(ns ouch-it-hurts.query-builder.utils
  (:require [clojure.set :refer [intersection]]
            [clojure.string :as str]
            [ouch-it-hurts.query-builder.ops :as ops]))

(defn keyword->str [kw]
  (str (when-let [ns (namespace kw)] (str ns "."))  (name kw)))

(defn- qb-operator [k v]
  (cond
    (and (map? v) (not-empty (intersection (set (keys v)) #{:from :to}))) ops/between
    (and (map? v) (contains? v :pattern)) ops/like
    (coll? v) ops/_in
    :else ops/eq))

(defn map->where [combinator m]
  (if (= 1 (count m))
    (let [[k v] (first m)]
      ((qb-operator k v) k v))
    (reduce (fn [acc [k v]]
              (conj acc ((qb-operator k v) k v))) [combinator] m)))

(defn map->order-by [orders]
  (reduce (fn [acc [k v]] (conj acc [k v])) [] orders))

(defn sql-date->local-date [date]
  (-> date
      (.getTime)
      (java.time.Instant/ofEpochMilli)
      (.atZone (java.time.ZoneId/systemDefault))
      (.toLocalDate)))

(defn sql-date->local-date-time [date]
  (-> date
      (.getTime)
      (java.time.Instant/ofEpochMilli)
      (.atZone (java.time.ZoneId/systemDefault))
      (.toLocalDateTime)))
