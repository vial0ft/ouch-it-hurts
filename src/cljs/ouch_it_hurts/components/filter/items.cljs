(ns ouch-it-hurts.components.filter.items
  (:require [ouch-it-hurts.utils.datetime-utils :as dtu]
            [ouch-it-hurts.components.common.core :refer [FieldSet DatePicker ErrorSpan]]))

(defn- set-elems-value [elem-id-opt]
  (doseq [[id {:keys [value on-change]}]  elem-id-opt]
    (when-let [elem (js/document.getElementById id)]
      (set! (.-value elem) value)
      (on-change nil))))

(defn DateRangeField
  [{:keys [key from to error]} legend]
  (let [from-part-key (str key "-range-start")
        to-part-key (str key "-range-end")]
    (fn [{:keys [error]}]
      [FieldSet legend
       [:div.date-range-block
        [DatePicker {:key from-part-key
                     :input {:on-change (:on-change from)}} "From: "]
        [DatePicker {:key to-part-key
                     :input {:on-change (:on-change to)}} "To: "]
        [:button {:type :reset
                  :on-click #(set-elems-value {from-part-key {:value "" :on-change (:on-change from)}
                                               to-part-key {:value "" :on-change (:on-change to)}})}  "Clear"]]
       [ErrorSpan error]])))

(defn CheckboxButton [{:keys [key opt]} label]
  [:div
   [:label {:for key}
    [:input  (merge
              {:id key
               :name key
               :type "checkbox"}
              opt)]
    [:span label]]])
