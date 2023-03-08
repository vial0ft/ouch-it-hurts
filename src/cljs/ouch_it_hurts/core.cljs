(ns ouch-it-hurts.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [ouch-it-hurts.components.filter.filter-form :refer [FilterForm]]
   [ouch-it-hurts.components.footer :refer [Footer]]
   [ouch-it-hurts.components.header :refer [Header]]
   [ouch-it-hurts.components.table-block :refer [TableBlock]]
   [ouch-it-hurts.components.patients-table-container :refer [PatientsTableContainer]]
   )
  )


(enable-console-print!)



;; -------------------------
;; States



(defn MainPage []
  [:div
   [Header]
   [PatientsTableContainer
    FilterForm
    TableBlock]
   [Footer]
   ])

(d/render [MainPage]
          (js/document.getElementById "main"))

