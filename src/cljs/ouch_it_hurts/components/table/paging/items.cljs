(ns ouch-it-hurts.components.table.paging.items
  (:require [ouch-it-hurts.components.common.core :refer [Select]]))

(defn PageNumber [{:keys [id key opt]} label]
  [:div {:id id}
   [:label {:for key}
    [:input  (merge
              {:id key
               :name key
               :type "checkbox"}
              opt)]
    [:span label]]])

(defn SkippedNumber []
  [:div {:id "paging-skipped-number-button"}
   [:label [:span "..."]]])

(defn PageSizeSelector [page-size-state {:keys [options on-change]}]
  (fn [page-size-state {:keys [on-change]}]
    (let [current-page-size @page-size-state]
      [Select {:key "page-size"
               :options (let [default current-page-size
                              all (map (fn [n] {:value n :lable n}) options)]
                          (map
                           #(if (not= (:value %) default)
                              %
                              (assoc % :default true)) all))
               :on-change on-change}])))

