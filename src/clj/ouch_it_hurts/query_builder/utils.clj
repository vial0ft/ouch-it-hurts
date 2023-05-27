(ns ouch-it-hurts.query-builder.utils
  (:require [clojure.set :refer [intersection]]
            [clojure.string :as str]
            [ouch-it-hurts.query-builder.ops :as ops]))

(defn as-snake-name [value]
  (let [replacement-f #(str/replace % #"-" "_")]
    (cond
      (keyword? value) (if-let [ns (namespace value)]
                         (keyword ns (replacement-f (name value)))
                         (keyword (replacement-f (name value))))
      (string? value) (replacement-f value)
      :else value)))

(defn- qb-operator [k v]
  (cond
    (and (map? v) (not-empty (intersection (set (keys v)) #{:from :to}))) ops/between
    (and (map? v) (contains? v :pattern)) ops/like
    (coll? v) ops/in
    :else ops/eq))

(defn map->where [m column-names-converter]
  (->> m
       (map (fn [[k v]] ((qb-operator k v) (column-names-converter k) v)))
       (apply ops/_and)))

(defn map->order-by [orders column-names-converter]
  (let [res   (->> orders
                   (map (fn [[k v]] [(column-names-converter k) v]))
                   (flatten))]
    (if-not (empty? res) res nil)))

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
