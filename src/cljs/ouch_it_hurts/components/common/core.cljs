(ns ouch-it-hurts.components.common.core
  (:require [reagent.core :as r]
            [ouch-it-hurts.utils.datetime-utils :as dtu]))

(defn- on-change-wrapper [on-change]
  (fn [e]
    "catch on-change"
    (on-change (.-value (.-target e)))))

(defn Select [{:keys [key options on-change]}]
  (let [opts   (if (some #(:selected %) options) options
                   (cons {:value "default" :selected true :disabled true :lable "Select"} options))]
    [:select {:id key :on-change (on-change-wrapper on-change)}
     (for [opt opts]
       ^{:key (:value opt)} [:option (select-keys opt [:value :disabled :selected]) (:lable opt)])]))

(defn ErrorSpan [err-msg]
  (fn [err-msg]
    [:div.error-message (when err-msg err-msg)]))

(defn LabledField [{:keys [key class lable input error]}]
  (fn [{:keys [key class lable input error]}]
    [:div
     [:div {:class class}
      [:label {:for key :class (:class lable)} (:text lable)]
      [:input
       (merge input
              {:id key
               :name key
               :on-change (on-change-wrapper (:on-change input))})]]
     [ErrorSpan error]]))

(defn LabledSelectField [{:keys [key class lable error]} select]
  (fn [{:keys [error]}]
    [:div
     [:div {:class class}
      [:label {:for key :class (:class lable)} (:text lable)]
      select
      [ErrorSpan error]]]))

(defn FieldSet [legend & fields]
  (fn [legend & fields]
  [:div
   [:fieldset
    [:legend legend]
    fields]]))

(defn SingleFieldSet
  [{:keys [key input error]} legend]
  (fn [{:keys [key input error]}]
    [FieldSet legend
     [:div
      [:input (merge input
                     {:id key
                      :name key
                      :on-change (on-change-wrapper (:on-change input))})]
      [ErrorSpan error]]]))

(defn DatePicker [{:keys [key class label input]}]
  [LabledField {:key key
                :class "date-range-item"
                :lable label
                :input (merge
                        {:type "date"
                         :min "1900-01-01"
                         :max (-> (dtu/start-of-date) (dtu/to-date))}
                        input)}])

(defn CloseButton [{:keys [on-click]}]
  [:input.close-button
   {:type "button"
    :on-click on-click
    :value "×"}])

(defn Modal [body close-action]
  (let [ref (r/atom nil)]
    (fn [body close-action]
      [:<>
       [:div
        [:div.modal-wrapper
         [:div.modal {:ref #(reset! ref %)
                      :on-click #(.-stopPropagation %)}
          [CloseButton {:on-click close-action}]
          body]]]])))

