(ns ouch-it-hurts.components.modal.core
  (:require [reagent.core :as r]
            [re-frame.core :as re :refer [subscribe]]
            [ouch-it-hurts.components.common.core :refer [Modal]]))

(defn PatientModal []
  (fn []
    (let [{:keys [form args]} @(subscribe [:modal/info])]
      (when form
        [Modal [form args] #(re/dispatch [:close-modal])]))))
