(ns ouch-it-hurts.components.table.core
  (:require
   [ouch-it-hurts.components.table.items :refer [OrderedHeaderCell RowCell]]
   [ouch-it-hurts.components.table.paging.core :refer [Paging]]
   [reagent.core :as r]))

(defn- ids-cursor [selected-ids-store]
  (fn
    ([] (r/cursor selected-ids-store [:ids]))
    ([id] (r/cursor selected-ids-store [:ids id]))))

(defn- select-all-handler[selected-ids-store all-ids]
  (fn [e]
    (let [all-checked? (.-checked (.-target e))]
      (reset! selected-ids-store {:all-selected? all-checked?
                                  :ids (reduce #(assoc %1 %2 all-checked?) {} all-ids)})
      )))

(defn- select-row-handler [selected-ids-store all-ids-count]
  (let [all-selected? (r/cursor selected-ids-store [:all-selected?])
        ids (ids-cursor selected-ids-store)]
    (fn [e]
      (let [id (int (.-id (.-target e)))]
        (swap! (ids id) not)
        (case [@(ids id) @all-selected?]
          [false true] (reset! all-selected? false)
          [true false] (when-not (some #(= % false) (vals @(ids)))
                         (if (= all-ids-count (count @(ids)))
                           (reset! all-selected? true)))
          :do-nothing)
        ))))

(defn- order-for-key [key ordering-atom]
  (fn [order]
    (if (nil? order)
      (swap! ordering-atom #(dissoc % key))
      (reset! (r/cursor ordering-atom [key]) order))))


(defn- HeaderRow [selected-ids-store all-ids ordering-state]
  (list
   [:div.patients-info-table-header-grid-item
    [:input {:id "all-select"
             :type "checkbox"
             :checked @(r/cursor selected-ids-store [:all-selected?])
             :on-change (select-all-handler selected-ids-store all-ids)}]]
   [OrderedHeaderCell {:value "Id"  :on-click (order-for-key :id ordering-state)}]
   [OrderedHeaderCell {:value "First name" :on-click (order-for-key :first-name ordering-state)}]
   [OrderedHeaderCell {:value "Middle name" :on-click (order-for-key :middle-name ordering-state)}]
   [OrderedHeaderCell {:value "Second name" :on-click (order-for-key :second-name ordering-state)}]
   [OrderedHeaderCell {:value "Sex" :on-click (order-for-key :sex ordering-state)}]
   [OrderedHeaderCell {:value "Birth Date" :on-click (order-for-key :birth-date ordering-state)}]
   [OrderedHeaderCell {:value "Address" :on-click (order-for-key :address ordering-state)}]
   [OrderedHeaderCell {:value "CHI number" :on-click (order-for-key :oms ordering-state)}]))


(defn- TableRow [selected-ids-store all-ids-count {:keys [id first-name middle-name second-name sex birth-date address oms deleted]}]
  (let [class (if-not deleted "patients-info-table-grid-item" "patients-info-table-grid-deleted-item")]
  (list
   [RowCell {:class class :value [:input {:id id
             		             :type "checkbox"
             		             :checked @(r/cursor selected-ids-store [:ids id])
             	   	           :on-change (select-row-handler selected-ids-store all-ids-count)}]}]
   [RowCell {:class class :value id }]
   [RowCell {:class class :value first-name }]
   [RowCell {:class class :value middle-name }]
   [RowCell {:class class :value second-name }]
   [RowCell {:class class :value sex }]
   [RowCell {:class class :value birth-date }]
   [RowCell {:class class :value address }]
   [RowCell {:class class :value oms }]
   )))

(defn- transform-to-rows [selected-ids-store all-ids records]
    (transduce
     (map (fn [r] (TableRow selected-ids-store (count all-ids) r)))
     into [] records))


(defn TableBlock [patients-info sorting paging]
    (fn [patients-info sorting paging]
      (let [selected-ids-store (r/cursor patients-info [:selected-ids])
            {:keys [data total]} @(r/cursor patients-info [:table-info])
            all-ids (map :id data)]
        [:div
         (-> [:div.patient-info-table-grid-container]
             (into (HeaderRow selected-ids-store all-ids sorting))
             (into (transform-to-rows selected-ids-store all-ids data)))
         [Paging total paging]]
        )))
