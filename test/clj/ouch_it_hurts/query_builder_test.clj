(ns ouch-it-hurts.query-builder-test
  (:require
   [ouch-it-hurts.query-builder.core :refer :all]
   [ouch-it-hurts.query-builder.ops :refer :all]
   [ouch-it-hurts.query-builder.utils :as u]
   [ouch-it-hurts.helpers.core :as th]
   [next.jdbc :as jdbc]
   [next.jdbc.prepare :as p]
   [clojure.test :refer :all]))


(def ^:dynamic *cfg* nil)
(def ^:dynamic *ds*)

(defn- build-query-part [query-and-args]
  (first (build query-and-args)))


(defn- create-test-table [ds]
  (jdbc/execute! ds ["create table if not exists test_table (
 	test_field text NULL
   )"]))


(defn- clear-table [ds]
  (jdbc/execute! ds ["delete from test_table"]))


(defn- once [work]
  (let [cfg {
             :dbtype "postgresql"
             :dbname "postgres"
             :classname "org.postgresql.Driver"
             :user "postgres"
             :password "12345678"
             }
        container (-> (th/create-container cfg)
                      (th/start-container))
        config       (-> cfg
                      (assoc :host (:host container))
                      (assoc :port (get (:mapped-ports container) 5432)))
        datasource (jdbc/get-datasource config)]
    (create-test-table datasource)
    (binding [*cfg* config
              *ds*  datasource]
      (work))
    (th/stop-container container)))

(defn- each [work]
  (work)
  (clear-table *ds*))

(use-fixtures :once once)
(use-fixtures :each each)


(deftest select-test
  (testing
   "select fragment"
    (is (= "select foo" (build-query-part (select [:foo]))))
    (is (= "select foo.bar" (build-query-part (select [:foo/bar]))))
    (is (= "select foo as bar" (build-query-part (select [[:foo :as :bar]]))))
    (is (= "select foo as bar , baz as qwe" (build-query-part (select [[:foo :as :bar] [:baz :as :qwe]]))))
    (is (= "select 1" (build-query-part (select [(symbol "1")]))))
    (let [[query arg] (build (select [(eq :id 1)]))]
      (is (= "select ( id = ? )" query))
      (is (= 1 arg)))))

(deftest from-test
  (testing
   "from fragment"
    (is (= "from table" (build-query-part (from [:table]))))
    (is (= "from s.table" (build-query-part (from [:s/table]))))
    (is (= "from table1 as t1 , table2 as t2" (build-query-part (from [[:table1 :as :t1] [:table2 :as :t2]]))))))

(deftest where-test
  (testing
   "where fragment"
    (let [[query & args] (build (where [(_or (eq :id 1) (eq  :id 2) (eq  :id 3))]))]
      (is (= "where ( ( id = ? ) or ( id = ? ) or ( id = ? ) )" query))
      (is (= [1 2 3] args)))
    (let [[query & args] (build (where [(_and (eq :id 1) (eq  :id 2) (eq  :id 3))]))]
      (is (= "where ( ( id = ? ) and ( id = ? ) and ( id = ? ) )" query))
      (is (= [1 2 3] args)))
    (let [[query & args] (build (where [(_or (eq :id 1) (between :id 2 3))]))]
      (is (= "where ( ( id = ? ) or ( id between ? and ? ) )" query))
      (is (= [1 2 3] args)))))

(deftest where-in
  (testing
   "where `any` with nil-free seq"
    (let [[query  array] (build (where [(_any :id [1 2 3 4 5])]))]
      (is (= "where ( id = any( ? ) )" query))
      (is (= #{1 2 3 4 5}  (set array)))))

  (testing
   "where `in` with `nil` element in seq and one more not-nil element"
    (let [[query  array] (build (where [(_any :id [1 2 3 4 5 nil])]))]
      (is (= "where ( ( id is null ) or ( id = any( ? ) ) )" query))
      (is (= #{1 2 3 4 5}  (set array)))))

  (testing
   "where `in` with the only `nil` element in seq"
    (let [[query  args] (build (where [(_any :id [nil])]))]
      (is (= "where ( id is null )" query))
      (is (nil? args)))))

(deftest where-between
  (testing
   "where `between` for map where keys [`from` `to`] exist"
    (let [[query  & args] (build (where [(between :k {:from "a" :to "b"})]))]
      (is (= "where ( k between ? and ? )" query))
      (is (= ["a" "b"] args))))

  (testing
   "where `between` for map where only `from` exist"
    (let [[query  & args] (build (where [(between :k {:from "a"})]))]
      (is (= "where ( k >= ? )" query))
      (is (= ["a"] args))))

  (testing
   "where `between` for map where only `to` exist"
    (let [[query  & args] (build (where [(between :k {:to "a"})]))]
      (is (= "where ( k <= ? )" query))
      (is (= ["a"] args)))))

(deftest like-test
  (testing "where `like` and pattern `start-with`"
    (let [[query  & args] (build (where [(like :k {:pattern "a%"})]))]
      (is (= "where ( k like ? )" query))
      (is (= ["a%"] args))))

  (testing "where `like` and pattern `end-with`"
    (let [[query  & args] (build (where [(like :k {:pattern "%a"})]))]
      (is (= "where ( k like ? )" query))
      (is (= ["%a"] args))))

  (testing "where `like` and pattern `between`"
    (let [[query  & args] (build (where [(like :k {:pattern "%a%"})]))]
      (is (= "where ( k like ? )" query))
      (is (= ["%a%"] args)))))

(deftest ordering-test
  (testing
   "order by fragment"
    (is (= "order by id desc , date asc" (build-query-part (order-by [[:id :desc] [:date :asc]]))))))

(deftest limit-test
  (testing
   "limit fragment"
    (let [[query  & args] (build (limit 100))]
      (is (= "limit ?" query))
      (is (= [100] args)))))

(deftest offset-test
  (testing
   "offset fragment"
    (let [[query  & args] (build (offset 100))]
      (is (= "offset ?" query))
      (is (= [100] args)))))

(defn- loop-check [[expected & rest-expected] [actual & rest-actual]]
  (if (function? expected)
    (is (expected actual))
    (is (= expected actual)))
  (when-not (and (empty? rest-expected) (empty? rest-actual))
    (recur rest-expected rest-actual)))


(deftest complex-query-test
  (testing
      "complex query"
    (let [[query & args]  (build (-> (select [[:id :as :qwe]])
                                   (from [[:tablename :as :t] [:table2 :as :t2]])
                                   (where [(_and (_or (eq :id 1) (between
                                                                  :date
                                                                  (java.time.LocalDate/of 2023 01 01)
                                                                  (java.time.LocalDate/of 2023 01 02)))
                                                 (_any :name ["John" "Smith" "Alex" nil])
                                                 (like :name {:pattern "foo%"}))])
                                   (order-by [[:id :desc]])
                                   (offset 100)
                                   (limit 100)))
          expected-args [1
                         (fn [act] (.isEqual (java.time.LocalDate/of 2023 01 01) act))
                         (fn [act] (.isEqual (java.time.LocalDate/of 2023 01 02) act))
                         (fn [act] (= ["John" "Smith" "Alex"] (java.util.Arrays/asList act)))
                         "foo%"
                         100
                         100]]
      (loop-check expected-args args)
      (is (= (clojure.string/join " "
                                  ["select id as qwe"
                                   "from tablename as t , table2 as t2"
                                   "where ( ( ( id = ? ) or ( date between ? and ? ) )"
                                   "and ( ( name is null ) or ( name = any( ? ) ) )"
                                   "and ( name like ? ) )"
                                   "order by id desc"
                                   "offset ? limit ?"])
             query)))))

(deftest complex-query-use-utils
  (testing
   "Use `utils/map->where` and `utils/map->order` for building query"
    (let [[query & args] (build (-> (select [[:id :as :qwe]])
                                  (from [[:tablename :as :t] [:table2 :as :t2]])
                                  (where [(u/map->where _and {:id 1
                                                              :date {:from (java.time.LocalDate/of 2023 01 01)
                                                                     :to (java.time.LocalDate/of 2023 01 02)}
                                                              :name {:pattern "foo%"}} u/as-snake-name)])
                                  (order-by [(u/map->order-by {:id :desc} u/as-snake-name)])))
          expected-args [1
                         (fn [act] (.isEqual (java.time.LocalDate/of 2023 01 01) act))
                         (fn [act] (.isEqual (java.time.LocalDate/of 2023 01 02) act))
                         "foo%"]]
      (is (= (clojure.string/join " "
                                  ["select id as qwe"
                                   "from tablename as t , table2 as t2"
                                   "where ( ( id = ? ) and ( date between ? and ? )"
                                   "and ( name like ? ) )"
                                   "order by id desc"])
             query))
      (loop-check expected-args args))))


(deftest prevent-sql-injection
  (testing "Add records"
    (let [strings ["qwe" "qweqwe" "qqq" "er" "aq" "qa" "awwee"]]
      (with-open [con (jdbc/get-connection *ds*)
                  ps (jdbc/prepare con ["insert into test_table (test_field) values(?)"])]
        (run! #(.addBatch (p/set-parameters ps [%])) strings)
        (.executeBatch ps)
        (is (= (count (jdbc/execute! con ["select * from test_table"]))
               (count strings))))
    ))


  (testing "`or 1=1`"
    (let [injecting-codes ["qwe or 1=1" "qwe' or 1=1" "qwe\" or \"\"=\""]]
      (doseq [ic injecting-codes]
        (let [[query & args] (-> (select [:*])
                                 (from [:test_table])
                                 (where [(eq :test_field ic)])
                                 (build))
              result (with-open [con (jdbc/get-connection *ds*)
                                 ps  (jdbc/prepare con [query])]
                       (jdbc/execute! (p/set-parameters ps args)))]
          (is (zero? (count result))))
        )
      ))

  (testing "drop table"
    (let [injecting-code "qwe; DROP TABLE test_table"]
      (let [[query & args] (-> (select [:*])
                               (from [:test_table])
                               (where [(eq :test_field injecting-code)])
                               (build))
            result (with-open [con (jdbc/get-connection *ds*)
                               ps  (jdbc/prepare con [query])]
                     (jdbc/execute! (p/set-parameters ps args)))])
      (is (pos-int? (count (with-open [con (jdbc/get-connection *ds*)]
                      (jdbc/execute! con ["select * from test_table"])))))))

  (testing "comment"
    (let [injecting-code "qwe'--"]
      (let [[query & args] (-> (select [:*])
                               (from [:test_table])
                               (where [(_or (eq :test_field injecting-code) (eq :test_field "qweqwe"))])
                               (build))
            result (with-open [con (jdbc/get-connection *ds*)
                               ps  (jdbc/prepare con [query])]
                     (jdbc/execute! (p/set-parameters ps args)))]
        (is (= (count result) 1)))
      ))
  )
