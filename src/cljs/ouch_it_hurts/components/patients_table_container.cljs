(ns ouch-it-hurts.components.patients-table-container
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.filter.core :refer [FilterForm]]
            [ouch-it-hurts.components.table.core :refer [TableBlock]]
            [ouch-it-hurts.api :as api]))



(def patients-info (r/atom []))


;; -------------------------
;; States


(defn error-handler [error]
  (fn[err]
    (reset! error {:ok? false
                   :message err})))

(defn result-handler [store opt]
  (fn [resp]
    (reset! (r/cursor opt [:error]) {:ok? true})
    (reset! patients-info resp)))

(defn fetch-patients-info [store opt]
  (api/fetch-patients-info
   (select-keys opt [:filters :offset :limit :sorting])
   (result-handler store opt)
   (error-handler (r/cursor opt [:error]))))

(defn PatientsTableContainer [app-state]
    (fetch-patients-info patients-info @app-state)
    (fn [app-state]
      (fetch-patients-info patients-info @app-state)
      [:div
       [FilterForm (r/cursor app-state [:filters])]
       [TableBlock patients-info (r/cursor app-state [:sorting])]]
      ))
