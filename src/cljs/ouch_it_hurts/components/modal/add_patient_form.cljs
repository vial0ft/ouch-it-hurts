(ns ouch-it-hurts.components.modal.add-patient-form
  (:require [ouch-it-hurts.components.common.core :refer [SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField ErrorSpan]]
            [ouch-it-hurts.utils.state :as s]
            [ouch-it-hurts.utils.datetime-utils :as dtu]))

(defn AddPatientForm [modal-state {:keys [add-callback]}]
  (let [[get-value reset-value] (s/use-state {})]
   [:div {:style {:margin "20px"}}
    [:h1 "New Patient's Info"]
    [:form {:on-submit (fn [e]
                         (println @(get-value))
                         (add-callback @(get-value))
                         (reset! modal-state {:visible? false})
                         (.preventDefault e))}
     [FieldSet "Patient name"
      [LabledField {:key "first-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "First name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(reset-value [:first-name] %)}}]
      [LabledField {:key "middle-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "Middle name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(reset-value [:middle-name] %)}}]
      [LabledField {:key "second-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "Second name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(reset-value [:second-name] %)}}]]
     [FieldSet "Sex"
       [Select {:key "sex"
                :options [{:value :male :lable "Male"}
                          {:value :female :lable "Female"}
                          {:value :other :lable "Other"}]
                :on-change #(reset-value [:sex] %)}]]
     [FieldSet "Birth date"
      [DatePicker {:key "birth-date"
                   :input {:on-change #(reset-value [:birth-date] (dtu/parse-date %))}}]]
     [SingleFieldSet
      {:key "address"
       :input {:type "text"
               :style {:width "100%"}
               :on-change #(reset-value [:address] %)
               }}
      "Address"]
     [SingleFieldSet
      {:key "cmi"
       :input {:type "text"
               :style {:width "100%"}
               :on-change #(reset-value [:oms] %)}
       ;;:error @(filter-form-cursor [:oms :error])
       }
      "CMI number"]
     [ErrorSpan @(get-value [:error])]
     [:button.filter-form-button {:type :submit} "Send"]
     ]
   ]))
