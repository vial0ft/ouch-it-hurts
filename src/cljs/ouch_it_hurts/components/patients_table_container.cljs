(ns ouch-it-hurts.components.patients-table-container
  (:require [reagent.core :as r]
            [re-frame.core :as re :refer [subscribe]]
            [ouch-it-hurts.components.filter.core :refer [FilterForm]]
            [ouch-it-hurts.components.table.core :refer [TableBlock]]
            [ouch-it-hurts.components.modal.core :refer [PatientModal]]
            [ouch-it-hurts.components.buttons-line.core :refer [ButtonsLine]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]))

(defn PatientsTableContainer []
    (fn []
      (let [patients-info @(subscribe [:table/info])]
        [:div.patient-table-container
         [FilterForm]
         [TableBlock patients-info]
         [ButtonsLine (get-in patients-info [:patients :data])]
         [PatientModal]])))
