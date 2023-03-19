(ns ouch-it-hurts.components.table-block
  (:require [goog.string :as gstr]
            [reagent.core :as r]))



(def ^:private state (r/atom {:ids {}
                              :all-selected? false
                              :all-ids #{}}))

(def ^:private all-select-key "all-select")


(defn- state-cursor [& path]
  (r/cursor state path))


(defn- records-cursor
  ([] (state-cursor :ids))
  ([id] (state-cursor :ids id)))

(def all-selected-flag-cursor (state-cursor :all-selected?))
(def all-ids-cursor (state-cursor :all-ids))

(defn set-checked-for-all [checked?]
  (fn [old] (reduce #(assoc %1 %2 checked?) {} @all-ids-cursor)))

(defn select-all-handler [e]
  (let [all-checked? (.-checked (.-target e))]
    (reset! all-selected-flag-cursor all-checked?)
    (swap! (records-cursor) (set-checked-for-all all-checked?))))

(defn select-row-handler [e]
  (let [id (int (.-id (.-target e)))]
    (swap! (records-cursor id) not)
    (case [@(records-cursor id) @all-selected-flag-cursor]
      [false true] (reset! all-selected-flag-cursor false)
      [true false] (when-not (some #(= % false) (vals @(records-cursor)))
                     (if (= (count @all-ids-cursor) (count @(records-cursor)))
                       (reset! all-selected-flag-cursor true)))
      :do-nothing)
    ))

(comment
  (reset! (state-cursor :ids) {1 false})

   (count @(state-cursor :ids))

   (vec `(:asd))

   (some #(= % false)  (list true true true))
  )


(defn- HeaderRow []
  (list
   [:div.patients-info-table-header-grid-item
    [:input {:id all-select-key
             :type "checkbox"
             :checked @(state-cursor :all-selected?)
             :on-change select-all-handler}]]
   [:div.patients-info-table-header-grid-item "Id"]
   [:div.patients-info-table-header-grid-item "First name"]
   [:div.patients-info-table-header-grid-item "Middle name"]
   [:div.patients-info-table-header-grid-item "Second name"]
   [:div.patients-info-table-header-grid-item "Sex"]
   [:div.patients-info-table-header-grid-item "Birth Date"]
   [:div.patients-info-table-header-grid-item "Address"]
   [:div.patients-info-table-header-grid-item "CHI number"]))


(defn- TableRow [{:keys [id first-name middle-name second-name
                         sex birth-date address oms]}]
  (list
   [:div.patients-info-table-grid-item
    [:input {:id id
             :type "checkbox"
             :checked @(state-cursor :ids id)
             :on-change select-row-handler}]]
   [:div.patients-info-table-grid-item id]
   [:div.patients-info-table-grid-item first-name]
   [:div.patients-info-table-grid-item middle-name]
   [:div.patients-info-table-grid-item second-name]
   [:div.patients-info-table-grid-item sex]
   [:div.patients-info-table-grid-item birth-date]
   [:div.patients-info-table-grid-item address]
   [:div.patients-info-table-grid-item oms]
   ))

(defn- transform-to-rows [records]
  (transduce
   (map (fn [r] (TableRow r)))
   into [] records))


(defn TableBlock [patients-info]
  (reset! (state-cursor :all-ids) (apply hash-set (map :id patients-info)))
  (fn [patients-info]
      [:div
       [:p {:hidden false } @state]
       (-> [:div.patient-info-table-grid-container]
           (into (HeaderRow))
           (into (transform-to-rows patients-info)))
       ]
      ))
