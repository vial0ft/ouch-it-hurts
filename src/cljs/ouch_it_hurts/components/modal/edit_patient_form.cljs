(ns ouch-it-hurts.components.modal.edit-patient-form
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.common.core :refer [SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField]]))


(defn- use-state[atom]
  [(fn [path] (r/cursor atom path)) (fn [path value] (reset! (r/cursor atom path) value))])

(defn EditPatientForm [modal-state  {:keys [patient-info edit-callback]}]
  (let [info (r/atom patient-info)
        [get-value reset-value] (use-state info)]
  [:div {:style {:margin "20px"}}
   [:h1 "Edit Patient's Info"]
   [:form {:on-submit (fn [e]
                        (.preventDefault e)
                        (edit-callback info))}
    [FieldSet "Patient name"
     [LabledField {:key "first-name"
                   :class "filter-form-block-item"
                   :lable {:class "filter-form-block-item-lable" :text "First name: "}
                   :input {:class "filter-form-block-item-text-input"
                           :type "text"
                           :default-value @(get-value [:first-name])
                           :on-change #(reset-value [:first-name] %)}}]
     [LabledField {:key "middle-name"
                   :class "filter-form-block-item"
                   :lable {:class "filter-form-block-item-lable" :text "Middle name: "}
                   :input {:class "filter-form-block-item-text-input"
                           :type "text"
                           :default-value @(get-value [:middle-name])
                           :on-change #(reset-value [:middle-name] %)}}]
     [LabledField {:key "second-name"
                   :class "filter-form-block-item"
                   :lable {:class "filter-form-block-item-lable" :text "Second name: "}
                   :input {:class "filter-form-block-item-text-input"
                           :type "text"
                           :default-value @(get-value [:second-name])
                           :on-change #(reset-value [:second-name] %)}}]]
    [FieldSet "Sex"
     [Select {:key "sex"
              :options (let [default @(get-value [:sex])
                             all [{:value :male :lable "Male"}
                                  {:value :female :lable "Female"}
                                  {:value :other :lable "Other"}]]
                              (map
                               #(if (not= (:value %) default)
                                  %
                                  (assoc % :default true)) all))
              :on-change #(reset-value [:sex] %)}]]
    [FieldSet "Birth date"
     [DatePicker {:key "birth-date"
                  :input {:default-value @(get-value [:birth-date])
                          :on-change #(reset-value [:birth-date] %)}}]]
    [SingleFieldSet
     {:key "address"
      :input {:type "text"
              :style {:width "100%"}
              :default-value @(get-value [:address])
              :on-change #(reset-value [:address] %)}}
     "Address"]
    [SingleFieldSet
     {:key "cmi"
      :input {:type "text"
              :style {:width "100%"}
              :default-value @(get-value [:oms])
              :on-change #(reset-value [:oms] %)}
       ;;:error @(filter-form-cursor [:oms :error])
      }
     "CMI number"]
    [:button.filter-form-button {:type :submit} "Send"]]])
  )
