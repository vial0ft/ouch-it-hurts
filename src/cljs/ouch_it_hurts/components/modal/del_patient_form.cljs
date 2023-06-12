(ns ouch-it-hurts.components.modal.del-patient-form
  (:require [reagent.core :as r]
            [goog.string :as gstr]
            [re-frame.core :as re]))

(defn DelPatientForm [ids]
  [:div {:style {:margin "20px"}}
   [:h1 "Delete patient's Info"]
   [:div (gstr/format "Are you sure you want to delete %s record(s)?" (count ids))]
   [:div
    [:button.filter-form-button {:on-click #(re/dispatch-sync [:delete-patient ids])} "Yes"]
    [:button.filter-form-button {:on-click #(re/dispatch [:close-modal])} "No"]]])
