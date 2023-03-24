(ns ouch-it-hurts.components.table.items
  (:require [reagent.core :as r]))



(def ^:private order-map {
                          "↕" nil
                          "↑" :desc
                          "↓" :asc
                          })

(defn- shift-order-value [curr]
  (case curr
    "↕" "↓"
    "↓" "↑"
    "↕"
    ))

(defn OrderedHeaderCell [{:keys [value on-click]}]
  (let [order-value (r/atom "↕")]
    (fn []
      [:div.patients-info-table-header-grid-item {:on-click #(on-click (-> (swap! order-value shift-order-value)
                                                                           (order-map)))}
       [:span value]
       [:span {:class "spaced"} @order-value]
       ]
      )))



(defn RowCell [{:keys [value]}]
  [:div.patients-info-table-grid-item value])
