(ns ouch-it-hurts.query-builder.core
  (:require [clojure.string :as s]
            [ouch-it-hurts.query-builder.ops :as ops]))


(defn- convert-value [value]
  (cond
    (and (string? value)
         (and (not (s/includes? value "'")) (not (s/includes? value "\"")))) (str "'" value "'")
    (keyword? value) (if-let [ns (namespace value)] (s/join "." [(namespace value) (name value)])
                                     (name value))
    :else (str value)))

(defn- aliasing [head rest]
  (if-not (= (first rest) :as) [head rest]
          [(str head (format " as %s" (convert-value (first (next rest)))))
           (next (next rest))]))

(defn- build-with-aliasing [[head & rest] join-f acc]
  (if-not head acc
      (let [[new-part new-rest] (aliasing (convert-value head) rest)]
        (recur new-rest join-f (join-f acc new-part)))))


(defn select-count
  ([& columns] (->> (build-with-aliasing columns #(conj %1 %2) [])
                   (s/join ", ")
                   (format "select count(%s)"))))

(defn select
  ([& columns] (->> (build-with-aliasing columns #(conj %1 %2) [])
                         (s/join ", ")
                         (str "select "))))


(defn from
  ([from-part]  (->> (build-with-aliasing from-part #(conj %1 %2) [])
                     (s/join ", ")
                     (str "from ")))
  ([query & from-part] (str query " " (from from-part))))


(defn- build-converted-expression [expr]
  (s/join " " (map convert-value expr)))


(defn- build-condition-part [[condition linked-word & rest]  acc]
  (if-not condition acc
          (if-not linked-word (conj acc (build-converted-expression condition))
                  (recur rest (conj acc (build-converted-expression condition) (convert-value linked-word)))
          )))

(defn where
  ([] "")
  ([conditions] (when conditions (->> (build-condition-part conditions []) (s/join " ") (str "where "))))
  ([query conditions] (str query " " (where conditions))))

(defn- asc-desc [field rest]
  (if-not (contains? #{:asc :desc "asc" "desc"} (first rest)) [(convert-value field) rest]
          [(str (convert-value field) " " (convert-value (first rest))) (next rest)]
  ))

(defn- build-ordering-part [[head & rest] acc]
  (if-not head acc
          (let [[new-head new-rest] (asc-desc head rest)]
            (recur new-rest (conj acc new-head))
            )))


(defn order-by
  ([] "")
  ([orders] (if orders (->> (build-ordering-part orders []) (s/join ", ") (str "order by ")) ""))
  ([query orders] (str query " " (order-by orders))))

(defn offset
  ([] "")
  ([offset-num] (if offset-num (str "offset " offset-num) ""))
  ([query offset-num] (str query " " (offset offset-num))))

(defn limit
  ([] "")
  ([limit-num] (if limit-num (str "limit " limit-num) ""))
  ([query limit-num] (str query " " (limit limit-num))))

