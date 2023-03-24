(ns ouch-it-hurts.components.table.core
  (:require
   [ouch-it-hurts.components.table.items :refer [OrderedHeaderCell RowCell]]
   [goog.string :as gstr]
   [reagent.core :as r]))



(defonce ^:private default-state {:ids {}
                                  :all-selected? false
                                  })

(def ^:private state (r/atom default-state))

(def ^:private all-select-key "all-select")


(defn- state-cursor [& path]
  (r/cursor state path))


(defn- records-cursor
  ([] (state-cursor :ids))
  ([id] (state-cursor :ids id)))

(def all-selected-flag-cursor (state-cursor :all-selected?))

(defn set-checked-for-all [all-ids checked?]
  (fn [old] (reduce #(assoc %1 %2 checked?) {} all-ids)))

(defn select-all-handler[all-ids]
  (fn [e]
    (let [all-checked? (.-checked (.-target e))]
      (reset! all-selected-flag-cursor all-checked?)
      (swap! (records-cursor) (set-checked-for-all all-ids all-checked?)))))

(defn select-row-handler [all-ids-count]
  (fn [e]
    (let [id (int (.-id (.-target e)))]
      (swap! (records-cursor id) not)
      (case [@(records-cursor id) @all-selected-flag-cursor]
        [false true] (reset! all-selected-flag-cursor false)
        [true false] (when-not (some #(= % false) (vals @(records-cursor)))
                       (if (= all-ids-count (count @(records-cursor)))
                         (reset! all-selected-flag-cursor true)))
        :do-nothing)
      )))




(defn- set-sort-ordering [key ordering]
  (reset! (state-cursor :sorting key) ordering))

(defn- order-for-key [key ordering-atom]
  (fn [order]
    (if (nil? order)
      (swap! ordering-atom #(dissoc % key))
      (reset! (r/cursor ordering-atom [key]) order))))


(defn- HeaderRow [all-ids ordering-state]
  (list
   [:div.patients-info-table-header-grid-item
    [:input {:id all-select-key
             :type "checkbox"
             :checked @(state-cursor :all-selected?)
             :on-change (select-all-handler all-ids)}]]
   [OrderedHeaderCell {:value "Id"  :on-click (order-for-key :id ordering-state)}]
   [OrderedHeaderCell {:value "First name" :on-click (order-for-key :first-name ordering-state)}]
   [OrderedHeaderCell {:value "Middle name" :on-click (order-for-key :middle-name ordering-state)}]
   [OrderedHeaderCell {:value "Second name" :on-click (order-for-key :second-name ordering-state)}]
   [OrderedHeaderCell {:value "Sex" :on-click (order-for-key :sex ordering-state)}]
   [OrderedHeaderCell {:value "Birth Date" :on-click (order-for-key :birth-date ordering-state)}]
   [OrderedHeaderCell {:value "Address" :on-click (order-for-key :address ordering-state)}]
   [OrderedHeaderCell {:value "CHI number" :on-click (order-for-key :oms ordering-state)}]))


(defn- TableRow [all-ids-count {:keys [id first-name middle-name second-name
                         sex birth-date address oms]}]
  (list
   [RowCell {:value [:input {:id id
             		             :type "checkbox"
             		             :checked @(state-cursor :ids id)
             	   	           :on-change (select-row-handler all-ids-count)}]}]
   [RowCell {:value id }]
   [RowCell {:value first-name }]
   [RowCell {:value middle-name }]
   [RowCell {:value second-name }]
   [RowCell {:value sex }]
   [RowCell {:value birth-date }]
   [RowCell {:value address }]
   [RowCell {:value oms }]
   ))

(defn- transform-to-rows [all-ids records]
    (transduce
     (map (fn [r] (TableRow (count all-ids) r)))
     into [] records))


(defn TableBlock [patients-info sorting]
    (fn [patients-info sorting]
      (let [patients-info-records @patients-info
            all-ids (map :id patients-info-records)]
        [:div
         [:p {:hidden false } @state]
         (-> [:div.patient-info-table-grid-container]
             (into (HeaderRow all-ids sorting))
             (into (transform-to-rows all-ids patients-info-records)))
         [:div "Paging"]
         [:span ""]
         ]
        )))


(comment
  (count  '(1 2 3 4 5) )
  )
