(ns ouch-it-hurts.helpers.gens
  (:require [clojure.test.check.generators :as gen]
            [clojure.string :as s]
            [ouch-it-hurts.specs :as sp]))




(defn oms-gen []
  (s/join (gen/generate (gen/vector gen/nat sp/oms-length) 9)))



