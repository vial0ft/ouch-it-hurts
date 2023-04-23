(ns ouch-it-hurts.components.patients-table-container
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.filter.core :refer [FilterForm]]
            [ouch-it-hurts.components.table.core :refer [TableBlock]]
            [ouch-it-hurts.components.modal.core :refer [PatientModal]]
            [ouch-it-hurts.components.buttons-line.core :refer [ButtonsLine]]
            [ouch-it-hurts.components.modal.edit-patient-form :refer [EditPatientForm]]
            [ouch-it-hurts.components.modal.view-patient-form :refer [ViewPatientForm]]
            [ouch-it-hurts.api :as api]
            [ouch-it-hurts.specs :as specs]
            [ouch-it-hurts.utils.promises :as promises]
            [clojure.spec.alpha :as s]
            [clojure.string :refer [join]]
            [spec-tools.core :as st]))



(def patients-info (r/atom {:selected-ids {:ids {}
                                           :all-selected? false}
                            }))

;; -------------------------
;; States


(defn- error-handler [app-state]
    #(reset! (r/cursor app-state [:error]) {:ok? false
                                            :message (-> (:parse-error %)
                                                         (select-keys [:status-text :original-text]))}))

(defn- go-to-page [state pn] (reset! (r/cursor state [:paging :page-number]) pn))

(defn- handle-fetch-result [store]
  #(reset! store {:ids {} :selected-all false :table-info % }))

(defn- fetch-patients-info [store app-state]
  (println "do fetch")
  (when @(r/cursor app-state [:error :ok?])
    (let [[result details] (specs/confirm-if-valid
                            :ouch-it-hurts.specs/query-request
                            (select-keys @app-state [:filters :sorting :paging]))]
      (case result
        :ok (-> (promises/retry #(api/fetch-patients-info details) 5)
                (.then (handle-fetch-result store))
                (.catch (error-handler app-state)))
        (reset! (r/cursor app-state [:error])  {:ok? false
                                                :message (join "\n" details)})
        )
      ))
    )


(defn post-update-action [app-state store]
  #(if-not (= @(r/cursor app-state [:paging :page-number]) 1)
     (go-to-page app-state 1)
     (fetch-patients-info store app-state)))

(defn- add-callback [app-state store]
  (fn [patient-info]
    (-> (api/add-patient-info patient-info)
        (.then (post-update-action app-state store))
        (.catch (error-handler app-state)))
    ))


(defn- edit-callback [app-state store]
  (fn [patients-info]
    (-> (api/update-patient-info patients-info)
        (.then (post-update-action app-state store))
        (.catch (error-handler app-state)))
    ))

(defn- delete-callback [app-state store]
  (fn [ids]
    (println "to delete" ids)
    (-> (map api/delete-patient-info ids)
        (js/Promise.all)
        (.then (fn[r] (println "deleted" r)
                 r))
        (.then (post-update-action app-state store))
        (.catch (error-handler app-state)))
    ))

(defn- view-callback [app-state]
  (fn [id modal-state edit-callback]
    (-> (api/get-patient-info-by-id id)
        (.then #(reset! modal-state {:visible? true
                                     :form ViewPatientForm
                                     :args {:patient-info % :edit-callback edit-callback}}))
        (.catch (error-handler app-state)))
    ))

(defn PatientsTableContainer [app-state]
  (let [!modal (r/atom {:visible? false})]
    (fetch-patients-info patients-info app-state)
    (fn [app-state]
      (fetch-patients-info patients-info app-state)
      [:div
       [FilterForm #(swap! app-state (fn [cur] (-> cur (assoc :filters %) (assoc-in [:paging :page-number] 1))))]
       [TableBlock
        patients-info
        (r/cursor app-state [:sorting])
        (r/cursor app-state [:paging])]
       [ButtonsLine
        !modal
        (r/cursor patients-info [:selected-ids :ids])
        {:delete-callback (delete-callback app-state patients-info)
         :add-callback (add-callback app-state patients-info)
         :edit-callback (edit-callback app-state patients-info)
         :view-callback (view-callback app-state)}]
       [PatientModal !modal]]
      )))


