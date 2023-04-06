(ns ouch-it-hurts.components.patients-table-container
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.filter.core :refer [FilterForm]]
            [ouch-it-hurts.components.table.core :refer [TableBlock]]
            [ouch-it-hurts.components.modal.core :refer [PatientModal]]
            [ouch-it-hurts.components.buttons-line.core :refer [ButtonsLine]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.api :as api]))



(def patients-info (r/atom []))

;; -------------------------
;; States


(defn- error-handler [error]
  (fn[err]
    (reset! error {:ok? false
                   :message err})))

(defn- result-handler [store opt]
  (fn [resp]
    (reset! (r/cursor opt [:error]) {:ok? true})
    (reset! patients-info resp)))

(defn- fetch-patients-info [store opt]
  (println "do fetch")
  (api/fetch-patients-info
   (select-keys opt [:filters :offset :limit :sorting])
   (result-handler store opt)
   (error-handler (r/cursor opt [:error]))))

(defn- add-callback [app-state]
  (fn [patient-info]
    (api/add-patient-info
     patient-info
     (fn [_] (reset! (r/cursor app-state [:current-page]) 1))
     error-handler)))

(defn- edit-callback [app-state]
  (fn [patients-info]
    (api/update-patient-info
     patients-info
     (fn [_] (reset! (r/cursor app-state [:current-page]) 1))
     error-handler)
    ))

(defn- delete-callback [app-state]
  (fn [id]
    (api/delete-patient-info
     id
     (fn [_] (reset! (r/cursor app-state [:current-page]) 1))
     error-handler)))

(defn- view-callback []
  (fn [id modal-state edit-callback]
    (api/get-patient-info-by-id
     id
     (fn [patient-info] (reset! modal-state {:visible? true
                                             :form EditPatientForm
                                             :args {:patient-info patient-info
                                                    :edit-callback edit-callback}
                                        }))
     error-handler
     )))


(defn PatientsTableContainer [app-state]
  (let [patients-info (r/atom [])
        selected-ids (r/atom {:ids {} :all-selected? false})
        !modal (r/atom {:visible? false})
        fetch  (fn [patients-info app-state] (fetch-patients-info patients-info @app-state))]
    (fetch patients-info app-state)
    (fn [app-state]
      ;;(println @app-state)
      (fetch patients-info app-state)
      [:div
       [FilterForm (r/cursor app-state [:filters])]
       [TableBlock
        patients-info
        selected-ids
        (r/cursor app-state [:sorting])
        (r/cursor app-state [:current-page])
        (r/cursor app-state [:page-size])]
       [ButtonsLine !modal selected-ids {:delete-callback (delete-callback app-state)
                                         :add-callback (add-callback app-state)
                                         :edit-callback (edit-callback app-state)
                                         :view-callback (view-callback)}]
       [PatientModal !modal]]
      )))
