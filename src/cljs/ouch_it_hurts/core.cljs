(ns ouch-it-hurts.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [re-frame.core :as re :refer [subscribe]]
   [ouch-it-hurts.events]
   [ouch-it-hurts.effects]
   [ouch-it-hurts.subs]
   [ouch-it-hurts.components.footer :refer [Footer]]
   [ouch-it-hurts.components.header :refer [Header]]
   [ouch-it-hurts.components.common.core :refer [ErrorSpan]]
   [ouch-it-hurts.components.patients-table-container :refer [PatientsTableContainer]]))

(enable-console-print!)


;; -------------------------
;; States

(defn MainPage []
  (re/dispatch [:fetch-patients-info])
  (fn []
    [:div.root
     [Header]
     [ErrorSpan @(subscribe [:error-app])]
     [PatientsTableContainer]
     [Footer]]
    ))

(do
  (re/dispatch-sync [:initialize-db])
  (d/render [MainPage]
            (js/document.getElementById "main")))
