(ns ouch-it-hurts.components.filter.right-block.core
  (:require
   [ouch-it-hurts.components.filter.items :refer [DateRangeField]]
   [ouch-it-hurts.components.common.core :refer [SingleFieldSet]]
   [re-frame.core :as re :refer [subscribe]]))

(defn- change-key [key-path] (fn [new-value] (reset! key-path new-value)))

(defn RightBlock []
  [:div.filter-form-block
   [SingleFieldSet
    {:key "address"
     :input {:type "text"
             :style {:width "100%"}
             :on-change #(re/dispatch [:filters/change :address %])}}  "Address"]
   [SingleFieldSet
    {:key "oms"
     :input {:type "text"
             :style {:width "100%"}
             :on-change #(re/dispatch [:filters/change :oms %])}} "OMS number"]
   [DateRangeField
    {:key "birth-date"
     :from {:on-change #(re/dispatch [:filters/birth-date-period :from %])}
     :to {:on-change #(re/dispatch [:filters/birth-date-period :to %])}}
    "Birth date range"]])

