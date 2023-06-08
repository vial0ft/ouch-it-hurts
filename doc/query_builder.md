# query-builder (aka Qu-Bi)

[`query-builder`](https://github.com/vial0ft/ouch-it-hurts/tree/main/src/clj/ouch_it_hurts/query_builder) is utility for building fragments of SQL query. It support (for now) limited set of [operations](https://github.com/vial0ft/ouch-it-hurts/blob/main/src/clj/ouch_it_hurts/query_builder/ops.clj#L3) and only `select` query. Functions from [`core`](https://github.com/vial0ft/ouch-it-hurts/blob/main/src/clj/ouch_it_hurts/query_builder/core.clj) are able to create part of query or join previous part of the query with part that has been creating.


## Examples

```clojure
(ns example
  (:require [query-builder.core :refer [select]]
            [query-builder.ops :refer :all]))

  (build (-> (select [[:id :as :qwe]])
             (from [[:tablename :as :t] [:table2 :as :t2]])
             (where [(_and (_or (eq :id 1)
                                (between :date
                                         (java.time.LocalDate/of 2023 01 01)
                                         (java.time.LocalDate/of 2023 01 02)))
                           (_any :name ["John" "Smith" "Alex" nil])
                           (like :name {:pattern "foo%"}))])
             (order-by [[:id :desc]])
             (offset 100)
             (limit 100)))

;;("select id as qwe from tablename as t , table2 as t2 where ( ( ( id = ? ) or ( date between ? and ? ) ) and ( ( name is null ) or ( name = any( ? ) ) ) and ( name like ? ) ) order by id desc offset ? limit ?" 1 #object[java.time.LocalDate 0x48135409 "2023-01-01"] #object[java.time.LocalDate 0x1f7677ff "2023-01-02"] #object["[Ljava.lang.String;" 0x4c8a308b "[Ljava.lang.String;@4c8a308b"] "foo%" 100 100)
```
`build` returns sequence where the first is query and rest is arguments according order of usage.

Also it's able to use `utils/map->where` and `utils/map->order-by`. Map would transform to a condition `where` with `and` link word.
In this case `query-builder` expect **transformer** like `utils/as-snake-name` - it transforms `:first-name` or `"first-name"` to conventional name for data bases `first_name`:

```clojure
(ns example
  (:require [query-builder.core :refer [select]]
            [query-builder.ops :refer :all]
            [query-builder.utils :as u]))

(build (-> (select [[:id :as :qwe]])
           (from [[:tablename :as :t] [:table2 :as :t2]])
           (where [(u/map->where _and {:id 1
                                       :date {:from (java.time.LocalDate/of 2023 01 01)
                                              :to (java.time.LocalDate/of 2023 01 02)}
                                       :name {:pattern "foo%"}} u/as-snake-name)])
           (order-by [(u/map->order-by {:id :desc} u/as-snake-name)])))
;;("select id as qwe from tablename as t , table2 as t2 where ( ( id = ? ) and ( date between ? and ? ) and ( name like ? ) ) order by id desc" 1 #object[java.time.LocalDate 0x10930775 "2023-01-01"] #object[java.time.LocalDate 0x14a0c283 "2023-01-02"] "foo%")
```
