(ns ouch-it-hurts.spec-test
  (:require  [clojure.test :refer :all]
             [clojure.spec.alpha :as s]
             [ouch-it-hurts.specs :as sp]
             [clojure.test.check.generators :as gen]
             [ouch-it-hurts.helpers.gens :as tg]))

(deftest patient-info-spec-test
  (testing "`new-patient-info` could contain at least one field"
    (is (not (s/valid? :ouch-it-hurts.specs/new-patient-info {})))
    (is (s/valid? :ouch-it-hurts.specs/new-patient-info {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
                                                         :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))
                                                         :last-name (gen/generate (s/gen :ouch-it-hurts.specs/last-name))
                                                         :address (gen/generate (s/gen :ouch-it-hurts.specs/address))
                                                         :birth-date "2020-01-01"
                                                         :sex (gen/generate (s/gen sp/sex-enum))
                                                         :oms (tg/oms-gen)})))

  (testing "Existing `patient-info` could contain at least one field and must contain `id`
           Also `patient-info` might contain `deleted` flag"
    (is (not (s/valid? :ouch-it-hurts.specs/patient-info {})))
    (is (not (s/valid?
              :ouch-it-hurts.specs/patient-info
              {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
               :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))
               :last-name (gen/generate (s/gen :ouch-it-hurts.specs/last-name))
               :address (gen/generate (s/gen :ouch-it-hurts.specs/address))
               :birth-date "2020-01-01"
               :sex (gen/generate (s/gen sp/sex-enum))
               :oms (tg/oms-gen)})))
    (is (s/valid?
         :ouch-it-hurts.specs/patient-info
         {:id (gen/generate (s/gen pos-int?))
          :first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
          :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))
          :last-name (gen/generate (s/gen :ouch-it-hurts.specs/last-name))
          :address (gen/generate (s/gen :ouch-it-hurts.specs/address))
          :birth-date "2020-01-01"
          :sex (gen/generate (s/gen sp/sex-enum))
          :oms (tg/oms-gen)
          :deleted (gen/generate (s/gen :ouch-it-hurts.specs/deleted))}))))

(deftest query-request-spec-test
  (testing "`page-size` from `paging` should be over `page-size-limit`"
    (is (s/valid? :ouch-it-hurts.specs/page-size sp/page-size-limit))
    (is (s/valid? :ouch-it-hurts.specs/page-size (inc sp/page-size-limit))))


  (testing "`paging` field is require `filters` and `sorting` are optional"
    (is (s/valid? :ouch-it-hurts.specs/query-request {:paging (gen/generate (s/gen :ouch-it-hurts.specs/paging))}))
    (is (not (s/valid? :ouch-it-hurts.specs/query-request {})))
    (is (not (s/valid? :ouch-it-hurts.specs/query-request {:filters {}})))
    (is (not (s/valid? :ouch-it-hurts.specs/query-request {:sorting {}})))
    (is (s/valid?
         :ouch-it-hurts.specs/query-request
         {:filters {} :paging (gen/generate (s/gen :ouch-it-hurts.specs/paging))}))
    (is (s/valid?
         :ouch-it-hurts.specs/query-request
         {:sorting {} :paging (gen/generate (s/gen :ouch-it-hurts.specs/paging))})))

  (testing "`paging`: `page-number` and `page-size` must be pos-int"
    (is (s/valid?
         :ouch-it-hurts.specs/query-request
         {:paging {:page-size (gen/generate (s/gen :ouch-it-hurts.specs/page-size))
                   :page-number (gen/generate (s/gen :ouch-it-hurts.specs/page-number))}}))
    (is (not (s/valid?
              :ouch-it-hurts.specs/query-request
              {:paging {:page-size -1
                        :page-number (gen/generate (s/gen :ouch-it-hurts.specs/page-number))}})))
    (is (not (s/valid?
              :ouch-it-hurts.specs/query-request
              {:paging {:page-size (gen/generate (s/gen :ouch-it-hurts.specs/page-size))
                        :page-number -1}}))))

  (testing "`paging`: `page-number` and `page-size` are require"
    (is (s/valid?
         :ouch-it-hurts.specs/query-request
         {:paging {:page-size (gen/generate (s/gen :ouch-it-hurts.specs/page-size))
                   :page-number (gen/generate (s/gen :ouch-it-hurts.specs/page-number))}}))
    (is (not (s/valid?
              :ouch-it-hurts.specs/query-request
              {:paging {:page-number (gen/generate (s/gen :ouch-it-hurts.specs/page-number))}})))
    (is (not (s/valid?
              :ouch-it-hurts.specs/query-request
              {:paging {:page-size (gen/generate (s/gen :ouch-it-hurts.specs/page-size))}}))))

  (testing
      "`sorting` could contain one or several fields from `patient-info` as key of map
        and `:asc`|`:desc` as value of map"
    (let [valid-paging (gen/generate (s/gen :ouch-it-hurts.specs/paging))]
      (is (s/valid? :ouch-it-hurts.specs/query-request {:sorting {:id :asc} :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:sorting {:id :asc :first-name :desc} :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:sorting {:id :desc} :paging valid-paging}))
      (is (not (s/valid? :ouch-it-hurts.specs/query-request {:sorting {:id :something-else} :paging valid-paging})))
      (is (not (s/valid? :ouch-it-hurts.specs/query-request {:sorting {:unexpected-field :asc} :paging valid-paging})))))

  (testing
      "`filters` could contain one or several fields from `patient-info` as key of map:
       `first-name`, `second-name`, `address`, `oms`.
       Instead `sex`, `filters` should contain `sex-opts`.
       Instead `birth-date`,`filters` should contain `birth-date-period`.
       `filters` could contain `show-records-opts`."
    (let [valid-paging (gen/generate (s/gen :ouch-it-hurts.specs/paging))]
      (is (s/valid?
           :ouch-it-hurts.specs/query-request
           {:filters {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))} :paging valid-paging}))
      (is (s/valid?
           :ouch-it-hurts.specs/query-request
           {:filters {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
                      :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))}
            :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:filters {:sex-opts sp/sex-filter-opts} :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:filters {:birth-date-period {:from "2010-01-01"
                                                                                      :to "2012-01-01"}}
                                                        :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:filters {:show-records-opts (first sp/show-records-opts)}
                                                        :paging valid-paging}))
      (is (s/valid? :ouch-it-hurts.specs/query-request {:filters {:oms (tg/oms-gen)} :paging valid-paging}))
      )))


(deftest add-patient-info-form-spec-test
  (testing
      "Form must contain at least one field of `patient-info`"
    (is (not (s/valid? :ouch-it-hurts.specs/add-patient-form {})))
    (is (s/valid?
         :ouch-it-hurts.specs/add-patient-form
         {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))}))
    (is (s/valid?
         :ouch-it-hurts.specs/add-patient-form
         {:middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))}))
    (is (s/valid?
         :ouch-it-hurts.specs/add-patient-form
         {:address (gen/generate (s/gen :ouch-it-hurts.specs/address))}))
    (is (s/valid?
         :ouch-it-hurts.specs/add-patient-form
         {:first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
          :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))
          :last-name (gen/generate (s/gen :ouch-it-hurts.specs/last-name))
          :address (gen/generate (s/gen :ouch-it-hurts.specs/address))
          :birth-date "2020-01-01"
          :sex (gen/generate (s/gen sp/sex-enum))
          :oms (tg/oms-gen)}))))

(comment

  (s/valid?
   :ouch-it-hurts.specs/edit-patient-form
   {:id (s/gen pos-int?) :first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))})


  )


(deftest edit-patient-info-form-spec-test
  (testing
      "Form must contain at least one field of existing `patient-info`.
       Existing `patient-info` must contain `id` field"
    (is (not (s/valid? :ouch-it-hurts.specs/edit-patient-form {})))
    (let [id-gen (s/gen pos-int?)]
    (is (s/valid?
         :ouch-it-hurts.specs/edit-patient-form
         {:id (gen/generate id-gen) :first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))}))
    (is (s/valid?
         :ouch-it-hurts.specs/edit-patient-form
         {:id (gen/generate id-gen)
          :first-name (gen/generate (s/gen :ouch-it-hurts.specs/first-name))
          :middle-name (gen/generate (s/gen :ouch-it-hurts.specs/middle-name))
          :last-name (gen/generate (s/gen :ouch-it-hurts.specs/last-name))
          :address (gen/generate (s/gen :ouch-it-hurts.specs/address))
          :birth-date "2020-01-01"
          :sex (gen/generate (s/gen sp/sex-enum))
          :oms (tg/oms-gen)})))))

(deftest delete-patient-info-request-spec-test
  (testing
      "Delete request must contain `pos-int` as `id` of `patient-info`"
    (is (s/valid? :ouch-it-hurts.specs/delete-patient-request (gen/generate (s/gen pos-int?))))
    (is (not (s/valid? :ouch-it-hurts.specs/delete-patient-request (gen/generate (s/gen neg-int?)))))
    (is (not (s/valid? :ouch-it-hurts.specs/delete-patient-request nil)))
    (is (not (s/valid? :ouch-it-hurts.specs/delete-patient-request (gen/generate (s/gen string?)))))))
