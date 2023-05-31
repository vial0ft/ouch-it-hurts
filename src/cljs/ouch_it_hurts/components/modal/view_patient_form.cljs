(ns ouch-it-hurts.components.modal.view-patient-form
  (:require [ouch-it-hurts.components.common.core :refer [SingleFieldSet DatePicker CloseButton FieldSet LabledField Select LabledSelectField]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.utils.datetime-utils :as dtu]))

(defn ViewPatientForm [modal-state {:keys [patient-info edit-callback restore-callback]}]
  (println patient-info (string? (:birth-date patient-info)))
  [:div {:style {:margin "20px"}}
   [:h1 "Patient's Info"]
   [FieldSet "Patient name"
    [LabledField {:key "first-name"
                  :class "view-modal-block-item"
                  :lable {:class "view-modal-block-item-lable" :text "First name: "}
                  :input {:class "view-modal-block-item-text-input"
                          :type "text"
                          :disabled true
                          :value (:first-name patient-info)}}]
    [LabledField {:key "middle-name"
                  :class "view-modal-block-item"
                  :lable {:class "view-modal-block-item-lable" :text "Middle name: "}
                  :input {:class "view-modal-block-item-text-input"
                          :type "text"
                          :disabled true
                          :value (:middle-name patient-info)}}]
    [LabledField {:key "last-name"
                  :class "view-modal-block-item"
                  :lable {:class "view-modal-block-item-lable" :text "Last name: "}
                  :input {:class "view-modal-block-item-text-input"
                          :type "text"
                          :disabled true
                          :value (:last-name patient-info)}}]]
   [SingleFieldSet
    {:key "sex"
     :input {:type "text"
             :class "view-modal-block-item-text-input"
             :disabled true
             :value (:sex patient-info)}}
    "Sex"]
   [SingleFieldSet
    {:key "birth-date"
     :input {:type "text"
             :class "view-modal-block-item-text-input"
             :disabled true
             :value
             (when-let [birth-date (:birth-date patient-info)]
               (-> birth-date (dtu/parse-date) (dtu/to-date)))}}
    "Birth date"]
   [SingleFieldSet
    {:key "address"
     :input {:type "text"
             :class "view-modal-block-item-text-input"
             :disabled true
             :value (:address patient-info)}}
    "Address"]
   [SingleFieldSet
    {:key "cmi"
     :input {:type "text"
             :class "view-modal-block-item-text-input"
             :disabled true
             :value (:oms patient-info)}}
    "CMI number"]
   (when (:deleted patient-info)
     [:div
      [:span {:style {:color "red"}} "Record deleted editing unavailable"]
      [:button.filter-form-button {:on-click #(do
                                                (reset! modal-state {:visible? false})
                                                (restore-callback (:id patient-info)))} "Restore"]])
   [:button.filter-form-button {:disabled (:deleted patient-info)
                                :on-click #(reset! modal-state {:visible? true
                                                                :form EditPatientForm
                                                                :args {:patient-info patient-info
                                                                       :edit-callback edit-callback}})} "Edit"]
   [:button.filter-form-button {:on-click #(reset! modal-state {:visible? false})} "Ok"]])
