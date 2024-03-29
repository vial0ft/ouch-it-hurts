(ns ouch-it-hurts.components.modal.edit-patient-form
  (:require [ouch-it-hurts.components.common.core :refer [ErrorSpan SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField]]
            [ouch-it-hurts.utils.state :as s]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [clojure.string :refer [join]]
            [ouch-it-hurts.specs :as specs]
            [clojure.data :refer [diff]]
            [re-frame.core :as re]))

(defn EditPatientForm [patient-info]
  (let [[get-value reset-value] (s/use-state patient-info)]
    (fn [patient-info]
      [:div {:style {:margin "20px"}}
       [:h1 "Edit Patient's Info"]
       [:form {:on-submit (fn [e]
                            (.preventDefault e)
                            (let [[result details]
                                  (->> (update-vals @(get-value) #(if (= "" %) nil %))
                                       (merge patient-info)
                                       (specs/confirm-if-valid :ouch-it-hurts.specs/edit-patient-form))]
                              (case result
                                :ok (re/dispatch-sync [:edit-patient details])
                                :error (reset-value [:error] (join "\n" details))
                                :do-nothing)))}
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
         [LabledField {:key "last-name"
                       :class "filter-form-block-item"
                       :lable {:class "filter-form-block-item-lable" :text "Last name: "}
                       :input {:class "filter-form-block-item-text-input"
                               :type "text"
                               :default-value @(get-value [:last-name])
                               :on-change #(reset-value [:last-name] %)}}]]
        [FieldSet "Sex"
         [Select {:key "sex"
                  :options (let [default @(get-value [:sex])
                                 all [{:value "male" :lable "Male"}
                                      {:value "female" :lable "Female"}]]
                             (map #(if (= (:value %) default) (assoc % :selected true) %) all))
                  :on-change #(reset-value [:sex] %)}]]
        [FieldSet "Birth date"
         [DatePicker {:key "birth-date"
                      :input {:default-value
                              (when-let [birth-date @(get-value [:birth-date])]
                                (-> birth-date (dtu/parse-date) (dtu/to-date)))
                              :on-change #(reset-value [:birth-date] (-> (dtu/parse-date %) (dtu/to-date)))}}]]
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
                  :on-change #(reset-value [:oms] %)}}
         "CMI number"]
        [ErrorSpan @(get-value [:error])]
        [:button.filter-form-button {:type :submit} "Send"]]])))

