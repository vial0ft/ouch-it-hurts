(ns ouch-it-hurts.components.buttons-line.core
  (:require [goog.string :as gstr]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.components.modal.add-patient-form :refer [AddPatientForm]]
            [ouch-it-hurts.components.modal.del-patient-form :refer [DelPatientForm]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.api :as api]))



(defn ButtonsLine [modal selected-ids {:keys [add-callback del-callback edit-callback view-callback]}]
  ;;(println selected-ids)
  (let [[disable-show disable-del] (case (count @selected-ids)
                                                  0 [true true]
                                                  1 [false false]
                                                  [true false]
                                                  )]
  [:div
   [:button.filter-form-button
    {
     :disabled false
     :on-click #(view-callback (first (keys @selected-ids)) modal edit-callback)
     }
    "Show patient's info"
    ]
   [:button.filter-form-button
    {
     :on-click #(reset! modal {:visible? true
                               :form AddPatientForm
                               :args {:add-callback add-callback}})
     }
    "Add patient"
    ]
   [:button.filter-form-button
    {
     :disabled false
     :on-click #(reset! modal {:visible? true
                               :form DelPatientForm
                               :args {:del-callback del-callback}})
     }
    "Delete patient(s)"
    ]
   [:button.filter-form-button
    {
     :on-click #(reset! modal {:visible? true
                               :form EditPatientForm
                               :args {:patient-info {} :edit-callback (fn [_] println "Edit")}})
     }
    "Edit patient"
    ]
   ]
  ))
