(ns ouch-it-hurts.components.modal.core
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.common.core :refer [Modal]]))

(defn PatientModal [modal-state]
  (fn [modal-state]
    (let [{:keys [form args]} @modal-state]
      [Modal (r/cursor modal-state [:visible?])
       [form modal-state args]])))
