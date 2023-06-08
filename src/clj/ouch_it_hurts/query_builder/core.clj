(ns ouch-it-hurts.query-builder.core
  (:require [clojure.string :as s]
            [ouch-it-hurts.query-builder.ops :as ops]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.prepare :as p]))


(defn keyword->str [kw]
  (str (when-let [ns (namespace kw)] (str ns "."))  (name kw)))


(defn expr [expression]
  (cond
    (vector? expression)  (mapv expr expression)
    (keyword? expression) (symbol (keyword->str expression))
    (symbol? expression) expression
    :else {:arg expression}
    ))

(defn resolve-expression [expr [acc args :as all]]
  (cond
    (vector? expr) (map #(resolve-expression % all) expr)
    (map? expr) [(conj acc (symbol "?")) (conj args (:arg expr))]
    :else [(conj acc expr) args]
    ))

(defn build-expression-line [expressions]
  (flatten (interpose (symbol ",") (map expr expressions))))

(defn join-parts [acc source] (reduce  #(resolve-expression %2 %1) acc source))

(defn select [columns]
  (let [select-line (build-expression-line columns)]
    (join-parts [[(symbol "select")] []] select-line)))

(defn from
  ([from-part] (from [[][]] from-part))
  ([[query-part args] from-part]
   [(vec (concat query-part [(symbol "from")] (build-expression-line from-part))) args]))


(defn where
  ([where-part] (where [[][]] where-part))
  ([[query-part args] where-part]
   (let [where-line (build-expression-line where-part)]
     (join-parts [query-part args] (conj where-line (symbol "where"))))))

(defn order-by
  ([order-by-part] (order-by [[][]] order-by-part))
  ([[query-part args] order-by-part]
   (let [order-by-line (build-expression-line order-by-part)]
     [(vec (concat query-part [(symbol "order by")] order-by-line)) args])))

(defn limit
  ([limit-part] (limit [[][]] limit-part))
  ([[query-part args] limit-part]
   (let [limit-line (list {:arg limit-part})]
     (join-parts [query-part args] (conj limit-line (symbol "limit"))))))

(defn offset
  ([offset-part] (offset [[][]] offset-part))
  ([[query-part args] offset-part]
   (let [offset-line (list {:arg offset-part})]
     (join-parts [query-part args] (conj offset-line (symbol "offset"))))))

(defn build [[query args]]
    (concat [(clojure.string/join " " (map str query))] args))

