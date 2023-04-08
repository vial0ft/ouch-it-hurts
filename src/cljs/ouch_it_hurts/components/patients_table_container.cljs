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


(defn- error-handler [app-state]
  (fn[err]
    (reset! (r/cursor app-state [:error]) {:ok? false
                                           :message err})))


(defn- go-to-page [state pn]
  (reset! (r/cursor state [:paging :page-number]) pn))

(defn- result-handler [store app-state]
  (fn [resp]
   ;; (println "resp" resp)
    (when-not @(r/cursor app-state [:error :ok?])
      (reset! (r/cursor app-state [:error]) {:ok? true}))
    (reset! store resp)))

(defn- fetch-patients-info [store app-state]
  (println "do fetch")
  (let [filters-sorting (select-keys @app-state [:filters :sorting])
        paging (:paging @app-state)]
  (api/fetch-patients-info
   (merge filters-sorting paging)
   (result-handler store app-state)
   (error-handler app-state))))

(defn- add-callback [app-state]
  (fn [patient-info]
    (api/add-patient-info
     patient-info
     (fn [_] (go-to-page app-state 1))
     (error-handler app-state))))

(defn- edit-callback [app-state]
  (fn [patients-info]
    (api/update-patient-info
     patients-info
     (fn [_] (go-to-page app-state 1))
     (error-handler app-state))
    ))

(defn- delete-callback [app-state]
  (fn [ids]
    (println "to delete" ids)
    (api/delete-patient-info
     (first ids)
     (fn [_] (go-to-page app-state 1))
     (error-handler app-state))))

(defn- view-callback [app-state]
  (fn [id modal-state edit-callback]
    (api/get-patient-info-by-id
     id
     (fn [patient-info]
       (println patient-info)
       (reset! modal-state {:visible? true
                            :form ViewPatientForm
                            :args {:patient-info patient-info
                                   :edit-callback edit-callback}
                            }))
     (error-handler app-state)
     )))

(defn PatientsTableContainer [app-state]
  (let [
        selected-ids (r/atom {:ids {} :all-selected? false})
        !modal (r/atom {:visible? false})
        fetch  (fn [patients-info app-state] (fetch-patients-info patients-info app-state))]
    (fn [app-state]
     (fetch patients-info app-state)
      [:div
       [FilterForm (r/cursor app-state [:filters])]
       [TableBlock
        patients-info
        selected-ids
        (r/cursor app-state [:sorting])
        (r/cursor app-state [:paging])]
       [ButtonsLine !modal (r/cursor selected-ids [:ids]) {:delete-callback (delete-callback app-state)
                                                           :add-callback (add-callback app-state)
                                                           :edit-callback (edit-callback app-state)
                                                           :view-callback (view-callback app-state)}]
       [PatientModal !modal]]
      )))
