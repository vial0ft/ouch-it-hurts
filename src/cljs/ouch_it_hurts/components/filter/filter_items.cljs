(ns ouch-it-hurts.components.filter.filter-items
  (:require [ouch-it-hurts.utils.datetime-utils :as dtu]))


(defn LabledField [{:keys [key label-text input-type on-change]}]
  [:div.filter-form-block-item
   [:label.filter-form-block-item-lable {:for key } label-text]
   [:input {
            :id key
            :type "text"
            :name key
            :on-change on-change}]
   ])

(defn DateRangeField
  [{:keys [legend key on-change-from on-change-to]}]
  (let [from-part-key (str key "-range-start")
        to-part-key (str key "-range-end")]
    [:div
     [:fieldset
      [:legend legend]
      [:label {:for from-part-key} "From: "]
      [:input {:id from-part-key
               :name from-part-key
               :type "date"
               :min "1900-01-01"
               :max (-> (dtu/start-of-date) (dtu/to-date))
               :on-change on-change-from}]
      [:span " "]
      [:label {:for to-part-key} "To: "]
      [:input {:id to-part-key
               :name to-part-key
               :type "date"
               :min "1900-01-01"
               :max (-> (dtu/start-of-date) (dtu/to-date))
               :on-change on-change-to}]
      ]]
    ))

(defn SingleFieldSet
  [{:keys [key legend input-type on-change]}]
  [:div
   [:fieldset
    [:legend legend]
    [:input {
             :id key
             :type input-type
             :name key
             :style {:width "100%"}
             :on-change on-change}]]])


(defn FieldSet [legend  & fields]
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
