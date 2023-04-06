(ns ouch-it-hurts.components.modal.del-patient-form
  (:require [reagent.core :as r]
            [goog.string :as gstr]))


(defn DelPatientForm [modal-state {:keys [ids delete-callback]}]
  [:div
   [:h1 "Patient's Info"]
   [:div (gstr/format "Are you sure you want to delete % record(s)?" (count ids))]
   [:div
    [:button {:on-click #(do
                           (delete-callback ids)
                           (reset! modal-state {:visible? false}))} "Yes"]
     [:button {:on-click #(reset! modal-state {:visible? false})} "No"]
    ]
   ])
