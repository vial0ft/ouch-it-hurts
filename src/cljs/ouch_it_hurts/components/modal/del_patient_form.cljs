(ns ouch-it-hurts.components.modal.del-patient-form
  (:require [reagent.core :as r]
            [goog.string :as gstr]))


(defn DelPatientForm [modal-state {:keys [ids delete-callback]}]
  [:div {:style {:margin "20px"}}
   [:h1 "Delete patient's Info"]
   [:div (gstr/format "Are you sure you want to delete %s record(s)?" (count ids))]
   [:div
    [:button.filter-form-button {:on-click #(do
                                              (reset! modal-state {:visible? false})
                                              (delete-callback ids))} "Yes"]
    [:button.filter-form-button {:on-click #(reset! modal-state {:visible? false})} "No"]
    ]
   ])
