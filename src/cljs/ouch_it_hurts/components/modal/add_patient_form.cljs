(ns ouch-it-hurts.components.modal.add-patient-form
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.common.core :refer [SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField]]))





(defn AddPatientForm [modal-state {:keys [add-callback]}]
   [:div {:style {:margin "20px"}}
    [:h1 "New Patient's Info"]
    [:form {:on-submit (fn [e]
                         (add-callback)
                         (.preventDefault e))}
     [FieldSet "Patient name"
      [LabledField {:key "first-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "First name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(println %)}}]
      [LabledField {:key "middle-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "Middle name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(println %)}}]
      [LabledField {:key "second-name"
                    :class "filter-form-block-item"
                    :lable {:class "filter-form-block-item-lable" :text "Second name: "}
                    :input {:class "filter-form-block-item-text-input"
                            :type "text"
                            :on-change #(println %)}}]]
     [FieldSet "Sex"
       [Select {:key "sex"
                :options [{:value :male :lable "Male"}
                          {:value :female :lable "Female"}
                          {:value :other :lable "Other"}]
                :on-change #(println %)}]]
     [FieldSet "Birth date"
      [DatePicker {:key "birth-date"
                   :on-change #(println %)}]]
     [SingleFieldSet
      {:key "address"
       :input {:type "text"
               :style {:width "100%"}
               :on-change #(println %)
               }}
      "Address"]
     [SingleFieldSet
      {:key "cmi"
       :input {:type "text"
               :style {:width "100%"}
               :on-change #(println %)}
       ;;:error @(filter-form-cursor [:oms :error])
       }
      "CMI number"]
     [:button.filter-form-button {:type :submit} "Send"]
     ]
   ])
