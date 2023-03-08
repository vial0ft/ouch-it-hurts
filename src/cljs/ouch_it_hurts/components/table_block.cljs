(ns ouch-it-hurts.components.table-block
  (:require [goog.string :as gstr]))



(defn- HeaderRow []
  [[:div.patients-info-table-header-grid-item [:input {:type "checkbox"}]]
   [:div.patients-info-table-header-grid-item "Id"]
   [:div.patients-info-table-header-grid-item "Full name"]
   [:div.patients-info-table-header-grid-item "Sex"]
   [:div.patients-info-table-header-grid-item "Birth Date"]
   [:div.patients-info-table-header-grid-item "Address"]
   [:div.patients-info-table-header-grid-item "CHI number"]])


(defn- TableRow [{:keys [id first-name middle-name second-name
                         sex birth-date address oms]}]
  [[:div.patients-info-table-grid-item [:input {:type "checkbox"}]]
   [:div.patients-info-table-grid-item id]
   [:div.patients-info-table-grid-item (gstr/format "%s %s %s" first-name middle-name second-name)]
   [:div.patients-info-table-grid-item sex]
   [:div.patients-info-table-grid-item birth-date]
   [:div.patients-info-table-grid-item address]
   [:div.patients-info-table-grid-item oms]
   ])

(defn- transform-to-rows [records]
  (transduce
   (map (fn [r] (TableRow r)))
   into [] records))


(defn TableBlock [patients-info]
  (let [patients-info-rows (if (empty? patients-info) []
                               (->> patients-info
                                    (transform-to-rows)))
        _ (println patients-info-rows)]
    (-> [:div.patient-info-table-grid-container]
        (into (HeaderRow))
        (into patients-info-rows)
        ))
  )
