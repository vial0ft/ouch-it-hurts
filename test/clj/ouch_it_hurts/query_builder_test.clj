(ns ouch-it-hurts.query-builder-test
  (:require
   [ouch-it-hurts.query-builder.core :refer :all]
   [ouch-it-hurts.query-builder.ops :refer :all]
   [clojure.test :refer :all]))



(deftest query-builder-test
  (testing
      "select fragment"
    (is (= "select foo" (select :foo)))
    (is (= "select foo.bar" (select :foo/bar)))
    (is (= "select foo as bar" (select :foo :as :bar)))
    (is (= "select foo as bar, baz as qwe" (select :foo :as :bar :baz :as "qwe")))
    (is (= "select foo" (select "foo")))
    (is (= "select 1" (select 1)))
    )

  (testing
      "from fragment"
    (is (= "from table" (from [:table])))
    (is (= "from s.table" (from [:s/table])))
    (is (= "from table1 as t1, table2 as t2" (from [:table1 :as :t1 :table2 :as :t2])))
    )

  (testing
      "where fragment"
    (is (= "where id = 1 or id = 2 or id = 3" (where (_or (eq :id 1) (eq  :id 2) (eq  :id 3)))))
    (is (= "where id = 1 and id = 2 and id = 3" (where (_and (eq :id 1) (eq  :id 2) (eq  :id 3)))))
    (is (= "where id = 1 and id = 2 and id = 3" (where (_and (eq :id 1) (eq  :id 2) (eq  :id 3)))))
    (is (= "where id = 1 or id between 2 and 3" (where (_or (eq :id 1) (between :id 2 3)))))
    )

  (testing
      "order by fragment"
    (is (= "order by id desc, date asc" (order-by [:id :desc :date :asc])))
    )

  (testing
      "limit fragment"
    (is (= "limit 100" (limit 100)))
    )

  (testing
      "offset fragment"
    (is (= "offset 100" (offset 100)))
    )


  (testing
      "complex query"
    (is (= (clojure.string/join " "
                               ["select id as 'qwe'"
                                "from tablename as t, table2 as t2"
                                "where id = 1 or date between 2023-01-01 and 2023-01-02"
                                "order by id desc"
                                "offset 100 limit 100"]
                                )
           (-> (select :id :as "'qwe'")
               (from :tablename :as :t :table2 :as :t2)
               (where (_or (eq :id 1) (between
                                               :date
                                               (java.time.LocalDate/of 2023 01 01)
                                               (java.time.LocalDate/of 2023 01 02))))
               (order-by :id :desc)
               (offset 100)
               (limit 100)
               )
           )
        )
    )
  )
