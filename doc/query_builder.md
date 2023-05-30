# query-builder (aka Qu-Bi)

[`query-builder`](https://github.com/vial0ft/ouch-it-hurts/tree/main/src/clj/ouch_it_hurts/query_builder) is utility for building fragments of SQL query. It support (for now) limited set of [operations](https://github.com/vial0ft/ouch-it-hurts/blob/main/src/clj/ouch_it_hurts/query_builder/ops.clj#L3) and only `select` query. Functions from [`core`](https://github.com/vial0ft/ouch-it-hurts/blob/main/src/clj/ouch_it_hurts/query_builder/core.clj) are able to create part of query or join previous part of the query with part that has been creating.


## Examples

```clojure
(ns example
  (:require [query-builder.core :refer [select]]
            [query-builder.ops :refer :all]))

  (-> (select :id :as "\"qwe\"")
      (from :tablename :as :t :table2 :as :t2)
      (where (_and (_or (eq :id 1)
                        (between
                                :date
                                (str (java.time.LocalDate/of 2023 01 01))
                                (str (java.time.LocalDate/of 2023 01 02))))
             (in :name ["John" "Smith" "Alex" nil])
             (like :name {:pattern "foo%"})))
      (order-by [:id :desc])
      (offset 100)
      (limit 100))

;;"select id as \"qwe\" from tablename as t, table2 as t2 where ((id = 1) or (date between '2023-01-01' and '2023-01-02')) and ((name is null) or (name in ('John','Smith','Alex'))) and (name like 'foo%') order by id desc offset 100 limit 100"
```
Also it's able to use `utils/map->where` and `utils/map->order-by`. Map would transform to a condition `where` with `and` link word.
In this case `query-builder` expect **transformer** like `utils/as-snake-name` - it transforms `:first-name` or `"first-name"` to conventional name for data bases `first_name`:

```clojure
(ns example
  (:require [query-builder.core :refer [select]]
            [query-builder.ops :refer :all]
            [query-builder.utils :as u]))

(-> (select :id :as "\"qwe\"")
    (from :tablename :as :t :table2 :as :t2)
    (where (u/map->where {:id 1
						  :date {:from (str (java.time.LocalDate/of 2023 01 01))
                          :to (str (java.time.LocalDate/of 2023 01 02))}
                          :name {:pattern "foo%"}} u/as-snake-name))
    (order-by (u/map->order-by {:id :desc} u/as-snake-name)))

;;"select id as \"qwe\" from tablename as t, table2 as t2 where ((id = 1) or (date between '2023-01-01' and '2023-01-02')) and ((name is null) or (name in ('John','Smith','Alex'))) and (name like 'foo%') order by id desc offset 100 limit 100""select id as \"qwe\" from tablename as t, table2 as t2 where (id = 1) and (date between '2023-01-01' and '2023-01-02') and (name like 'foo%') order by id desc"
```
