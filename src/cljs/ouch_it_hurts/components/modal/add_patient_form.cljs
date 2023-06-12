(ns ouch-it-hurts.components.modal.add-patient-form
  (:require [ouch-it-hurts.components.common.core :refer [SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField ErrorSpan]]
            [ouch-it-hurts.utils.state :as s]
            [ouch-it-hurts.specs :as specs]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [re-frame.core :as re]
            [clojure.string :refer [join]]))

(defn AddPatientForm []
  (let [[get-value reset-value] (s/use-state {})]
    (fn []
      [:div {:style {:margin "20px"}}
       [:h1 "New Patient's Info"]
       [:form {:on-submit (fn [e]
                            (.preventDefault e)
                            (let [[result details]
                                  (->> (update-vals @(get-value) #(if (= "" %) nil %))
                                       (specs/confirm-if-valid :ouch-it-hurts.specs/add-patient-form))]
                              (case result
                                :ok (re/dispatch [:add-patient details])
                                (reset-value [:error] (join "\n" details)))))}
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
         [LabledField {:key "last-name"
                       :class "filter-form-block-item"
                       :lable {:class "filter-form-block-item-lable" :text "Last name: "}
                       :input {:class "filter-form-block-item-text-input"
                               :type "text"
                               :on-change #(reset-value [:last-name] %)}}]]
        [FieldSet "Sex"
         [Select {:key "sex"
                  :options [{:value "male" :lable "Male"}
                            {:value "female" :lable "Female"}]
                  :on-change #(reset-value [:sex] %)}]]
        [FieldSet "Birth date"
         [DatePicker {:key "birth-date"
                      :input {:on-change #(reset-value [:birth-date] (-> (dtu/parse-date %) (dtu/to-date)))}}]]
        [SingleFieldSet
         {:key "address"
          :input {:type "text"
                  :style {:width "100%"}
                  :on-change #(reset-value [:address] %)}}
         "Address"]
        [SingleFieldSet
         {:key "cmi"
          :input {:type "text"
                  :style {:width "100%"}
                  :on-change #(reset-value [:oms] %)}}
         "CMI number"]
        [ErrorSpan @(get-value [:error])]
        [:button.filter-form-button {:type :submit} "Send"]]])))

