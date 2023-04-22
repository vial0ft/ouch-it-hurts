(ns ouch-it-hurts.specs-test
  (:require [clojure.test :refer :all]
            [ouch-it-hurts.specs :as specs]))


(deftest add-patient-form-scehma-test
  (testing "Valid new patient info"
    (let [new-patient {:first-name "qwe"}
          [result details] (specs/confirm-if-valid :ouch-it-hurts.specs/add-patient-form new-patient)]
      (is (= result :ok))
      )
    )

  )










