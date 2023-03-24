(ns ouch-it-hurts.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [ouch-it-hurts.components.filter.core :refer [FilterForm]]
   [ouch-it-hurts.components.footer :refer [Footer]]
   [ouch-it-hurts.components.header :refer [Header]]
   [ouch-it-hurts.components.table.core :refer [TableBlock]]
   [ouch-it-hurts.components.patients-table-container :refer [PatientsTableContainer]]
   )
  )


(enable-console-print!)



(def app-state (r/atom {:filters {}
                        :sorting {:id :asc}
                        :offset 0
                        :limit 100
                        :error {:ok? true
                                :message ""}
                        }))
;; -------------------------
;; States



(defn MainPage []
  [:div
   [Header]
   [:p {:hidden @(r/cursor app-state [:error :ok?])} @(r/cursor app-state [:error :message])]
   [:p {:hidden false} @app-state]
   [PatientsTableContainer app-state]
   [Footer]
   ])

(d/render [MainPage]
          (js/document.getElementById "main"))

