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
                        :paging {:page-number 1
                                 :page-size 10}
                        :error {:ok? true
                                :message ""}
                        }))
;; -------------------------
;; States



(defn MainPage []
  [:div
   [Header]
   [:div {:hidden @(r/cursor app-state [:error :ok?])}
    [:span (str @(r/cursor app-state [:error :message]))]

    [:button {:style {:float "right"}
              :on-click #(reset! (r/cursor app-state [:error]) {:ok? true :message ""})}
    "Refresh"]]
   [:p {:hidden false} @app-state]
   [PatientsTableContainer app-state]
   [Footer]
   ])

(d/render [MainPage]
          (js/document.getElementById "main"))

