(ns ouch-it-hurts.components.buttons-line.core
  (:require [goog.string :as gstr]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.components.modal.add-patient-form :refer [AddPatientForm]]
            [ouch-it-hurts.components.modal.del-patient-form :refer [DelPatientForm]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.api :as api]))

(defn- selected-true [selected-ids]
  (filter #(= (val %) true) selected-ids))

(defn ButtonsLine [modal selected-ids {:keys [add-callback delete-callback edit-callback view-callback]}]
  (fn [modal selected-ids {:keys [add-callback del-callback edit-callback view-callback]}]
    (let [
          selected (selected-true @selected-ids)
          [disable-show disable-del] (case (count selected)
                                       0 [true true]
                                       1 [false false]
                                       [true false])]
      [:div
       [:button.filter-form-button
        {:disabled disable-show
         :on-click #(view-callback (first (keys selected)) modal edit-callback)}
        "Show patient's info"]
       [:button.filter-form-button
        {:on-click #(reset! modal {:visible? true
                                   :form AddPatientForm
                                   :args {:add-callback add-callback}})}
        "Add patient"]
       [:button.filter-form-button
        {:disabled disable-del
         :on-click #(reset! modal {:visible? true
                                   :form DelPatientForm
                                   :args {:ids (keys selected)
                                          :delete-callback delete-callback}})}
        "Delete patient(s)"]])))
