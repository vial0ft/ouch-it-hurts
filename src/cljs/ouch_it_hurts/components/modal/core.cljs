(ns ouch-it-hurts.components.modal.core
  (:require [reagent.core :as r]
            [goog.string :as gstr]
            [ouch-it-hurts.components.common.core :refer [Modal]]
            [ouch-it-hurts.components.modal.add-patient-form :refer [AddPatientForm]]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]))




(defn PatientViewModal [modal-state]
  (fn [modal-state]
    (let [visible? (r/cursor modal-state [:visible?])
          id (:ids @modal-state)]
      [Modal visible?
       [ViewPatientForm {}]
       ]
      )))


(defn PatientAddModal [modal-state]
  (fn [modal-state]
    (let [visible? (r/cursor modal-state [:visible?])]
      [Modal visible?
       [AddPatientForm]
       ]
      )))

(defn PatientEditModal [modal-state]
  (fn [modal-state]
    (let [visible? (r/cursor modal-state [:visible?])]
      [Modal visible?
       [EditPatientForm {}]
       ]
      )))


(defn PatientDeleteModal [modal-state delete-callback]
  (fn [modal-state delete-callback]
    (let [visible? (r/cursor modal-state [:visible?])
          ids (:ids @modal-state)]
      [Modal visible?
       [:div
        [:h1 "Patient's Info"]
        [:div (gstr/format "Are you sure you want to delete % record(s)?" (count ids))]
        [:div
         [:button {:on-click #(do
                                (delete-callback ids)
                                (reset! visible? false))} "Yes"]
         [:button {:on-click #(reset! visible? false)} "No"]
         ]
        ]]
      )))



(defn PatientModal [modal-state]
  (fn [modal-state]
    (let [{:keys [form args]} @modal-state]
      [Modal (r/cursor modal-state [:visible?])
       [form modal-state args]]
      )))
