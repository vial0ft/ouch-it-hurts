(ns ouch-it-hurts.query-builder-test
  (:require
   [ouch-it-hurts.query-builder.core :refer :all]
   [ouch-it-hurts.query-builder.ops :refer :all]
   [clojure.test :refer :all]))



(deftest select-test
  (testing
      "select fragment"
    (is (= "select foo" (select :foo)))
    (is (= "select foo.bar" (select :foo/bar)))
    (is (= "select foo as bar" (select :foo :as :bar)))
    (is (= "select foo as bar, baz as \"qwe\"" (select :foo :as :bar :baz :as "\"qwe\"")))
    (is (= "select 1" (select 1)))
    )
)

(deftest from-test
  (testing
      "from fragment"
    (is (= "from table" (from [:table])))
    (is (= "from s.table" (from [:s/table])))
    (is (= "from table1 as t1, table2 as t2" (from [:table1 :as :t1 :table2 :as :t2])))
    )
)

(deftest where-test
  (testing
      "where fragment"
    (is (= "where (id = 1) or (id = 2) or (id = 3)" (where (_or (eq :id 1) (eq  :id 2) (eq  :id 3)))))
    (is (= "where (id = 1) and (id = 2) and (id = 3)" (where (_and (eq :id 1) (eq  :id 2) (eq  :id 3)))))
    (is (= "where (id = 1) or (id between 2 and 3)" (where (_or (eq :id 1) (between :id 2 3)))))
    ))

(deftest where-in
  (testing
      "where `in` with nil-free seq"
    (is (= "where id in (1,2,3,4,5)" (where (in :id [1 2 3 4 5])))))

  (testing
      "where `in` with `nil` element in seq and one more not-nil element"
    (is (= "where (id is null) or (id in (1))" (where (in :id [1 nil])))))

  (testing
      "where `in` with the only `nil` element in seq"
    (is (= "where id is null" (where (in :id [nil])))))
)

(deftest where-between
  (testing
      "where `between` for map where keys [`from` `to`] exist"
    (is (= "where k between 'a' and 'b'" (where (between :k {:from "a" :to "b"}))))
    )

  (testing
      "where `between` for map where only `from` exist"
    (is (= "where k >= 'a'" (where (between :k {:from "a"}))))
    )


  (testing
      "where `between` for map where only `to` exist"
    (is (= "where k <= 'a'" (where (between :k {:to "a"}))))
    )
  )

(deftest ordering-test
  (testing
      "order by fragment"
    (is (= "order by id desc, date asc" (order-by [:id :desc :date :asc])))
    ))

(deftest limit-test
  (testing
      "limit fragment"
    (is (= "limit 100" (limit 100)))
    ))

(deftest offset-test
  (testing
      "offset fragment"
    (is (= "offset 100" (offset 100)))
    ))


(deftest complex-query-test
  (testing
      "complex query"
    (is (= (clojure.string/join " "
                               ["select id as \"qwe\""
                                "from tablename as t, table2 as t2"
                                "where ((id = 1) or (date between '2023-01-01' and '2023-01-02'))"
                                       "and ((name is null) or (name in ('John','Smith','Alex')))"
                                "order by id desc"
                                "offset 100 limit 100"]
                                )
           (-> (select :id :as "\"qwe\"")
               (from :tablename :as :t :table2 :as :t2)
               (where (_and (_or (eq :id 1) (between
                                               :date
                                               (str (java.time.LocalDate/of 2023 01 01))
                                               (str (java.time.LocalDate/of 2023 01 02))))
                            (in :name ["John" "Smith" "Alex" nil])))
               (order-by [:id :desc])
               (offset 100)
               (limit 100)
               )
           ))))
