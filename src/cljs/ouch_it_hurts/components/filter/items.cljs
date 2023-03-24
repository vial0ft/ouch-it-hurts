(ns ouch-it-hurts.components.filter.items
  (:require [ouch-it-hurts.utils.datetime-utils :as dtu]))



(defn- on-change-wrapper [on-change]
  (fn [e]
    "catch on-change"
    (on-change (.-value (.-target e)))
    ))

(defn- ErrorSpan [{:keys [error? message]}]
  [:span.error-message (if error? message "")])

(defn LabledField [{:keys [key label-text input-type on-change error]}]
  (fn [{:keys [error]}]
    [:div
     [:div.filter-form-block-item
      [:label.filter-form-block-item-lable {:for key } label-text]
      [:input.filter-form-block-item-text-input {
                                                 :id key
                                                 :type "text"
                                                 :name key
                                                 :on-change (on-change-wrapper on-change)}]]
     [ErrorSpan error]
   ]))

(defn- set-elems-value [elem-id-opt]
  (doseq [[id {:keys [value on-change]}]  elem-id-opt]
    (when-let [elem (js/document.getElementById id)]
      (set! (.-value elem) value)
      (on-change nil)
    )))

(defn DateRangeField
  [{:keys [legend key from to error]}]
  (let [from-part-key (str key "-range-start")
        to-part-key (str key "-range-end")]
    (fn [{:keys [error]}]
      [:div
       [:fieldset
        [:legend legend]
        [:div.date-range-block
         [:div.date-range-item
          [:label.date-range-lable {:for from-part-key} "From: "]
          [:input {:id from-part-key
                   :name from-part-key
                   :type "date"
                   :min "1900-01-01"
                   :max (-> (dtu/start-of-date) (dtu/to-date))
                   :on-change (on-change-wrapper (:on-change from))
                   }]]
         [:div.date-range-item
          [:label.date-range-lable {:for to-part-key} "To: "]
          [:input {:id to-part-key
                   :name to-part-key
                   :type "date"
                   :min "1900-01-01"
                   :max (-> (dtu/start-of-date) (dtu/to-date))
                   :on-change (on-change-wrapper (:on-change to))
                   }]]
         [:button {:type :reset
                   :on-click #(set-elems-value {
                                                from-part-key {:value "" :on-change (:on-change from)}
                                                to-part-key {:value "" :on-change (:on-change to)}
                                                })}
          "Clear"]]
        [ErrorSpan error]
      ]]
    )))

(defn SingleFieldSet
  [{:keys [legend input-type on-change error]}]
  (fn [{:keys [error]}]
    [:div
     [:fieldset
      [:legend legend]
      [:div
       [:input {
                :id key
                :type input-type
                :name key
                :style {:width "100%"}
                :on-change (on-change-wrapper on-change)}]
         [ErrorSpan error]
       ]]]))


(defn FieldSet [legend & fields]
  [:div
   [:fieldset
    [:legend legend]
    fields
    ]]
  )

(defn CheckboxButton [{:keys [key label opt]}]
  [:div {:id "ck-button"}
   [:label {:for key}
    [:input  (merge
              {:id key
               :name key
               :type "checkbox"}
              opt)]
   [:span label]
    ]]
  )
