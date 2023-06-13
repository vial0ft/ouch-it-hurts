(ns ouch-it-hurts.components.buttons-line.core
  (:require [re-frame.core :as re :refer [subscribe]]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.components.modal.add-patient-form :refer [AddPatientForm]]
            [ouch-it-hurts.components.modal.del-patient-form :refer [DelPatientForm]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]))

(defn- selected-true [selected-ids]
  (filter #(val %) selected-ids))

(defn ButtonsLine [patients]
  (fn [patients]
    (let [selected (filter #(:selected? %) patients)
          [disable-show disable-del] (case (count selected)
                                       0 [true true]
                                       1 [false false]
                                       [true false])]
      [:div.buttons-line
       [:button.filter-form-button
        {:disabled disable-show
         :on-click #(do
                      (re/dispatch-sync [:get-patient-by-id  (:id (first selected))])
                      (re/dispatch [:show-modal ViewPatientForm]))}
        "Show patient's info"]
       [:button.filter-form-button
        {:on-click #(re/dispatch [:show-modal AddPatientForm])}
        "Add patient"]
       [:button.filter-form-button
        {:disabled disable-del
         :on-click #(re/dispatch [:show-modal DelPatientForm (map :id selected)])}
        "Delete patient(s)"]])))
