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

(defn PageSizeSelector [{:keys [current options on-change]}]
  (fn [{:keys [on-change]}]
      [Select {:key "page-size"
               :options (let [default current
                              all (map (fn [n] {:value n :lable n}) options)]
                          (map #(if (= (:value %) default) (assoc % :selected true) %) all))
               :on-change on-change}]))

