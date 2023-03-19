(ns ouch-it-hurts.components.patients-table-container
  (:require [reagent.core :as r]))




;; -------------------------
;; States

(def ^:private patients-info (r/atom [{:id 1
                                       :first-name "Иванов"
                                       :second-name "Иван"
                                       :middle-name "Иванович"
                                       :birth-date "qweqwe"
                                       :address "asdasd"
                                       :sex "m"
                                       :oms "00000"}
                                      {:id 2
                                       :first-name "Иванов"
                                       :second-name "Иван"
                                       :middle-name "Иванович"
                                       :birth-date "qweqwe"
                                       :address "qqqq"
                                       :sex "m"
                                       :oms "00000"}
                                      ])
  )



(defn PatientsTableContainer [FilterBlock TableBlock]
  [:div
   [FilterBlock #(println "We got filter info: " %)]
   [TableBlock @patients-info]
   ]
  )

