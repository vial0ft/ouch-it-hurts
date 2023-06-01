(ns ouch-it-hurts.components.filter.right-block.core
  (:require
   [ouch-it-hurts.components.filter.items :refer [DateRangeField]]
   [ouch-it-hurts.components.common.core :refer [SingleFieldSet]]))

(defn- change-key [key-path] (fn [new-value] (reset! key-path new-value)))

(defn RightBlock [store-path-f]
  [:div.filter-form-block
   [SingleFieldSet
    {:key "address"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (store-path-f [:address :value]))}}  "Address"]
   [SingleFieldSet
    {:key "oms"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (store-path-f [:oms :value]))}} "CMI number"]
   [DateRangeField
    {:key "birth-date"
     :from {:on-change (change-key (store-path-f [:birth-date-period :value :from]))}
     :to {:on-change (change-key (store-path-f [:birth-date-period :value :to]))}}
    "Birth date range"]])
