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


(defn- TableRow [selected-ids-store all-ids-count {:keys [id first-name middle-name second-name sex birth-date address oms]}]
  (list
   [RowCell {:value [:input {:id id
             		             :type "checkbox"
             		             :checked @(r/cursor selected-ids-store [:ids id])
             	   	           :on-change (select-row-handler selected-ids-store all-ids-count)}]}]
   [RowCell {:value id }]
   [RowCell {:value first-name }]
   [RowCell {:value middle-name }]
   [RowCell {:value second-name }]
   [RowCell {:value sex }]
   [RowCell {:value birth-date }]
   [RowCell {:value address }]
   [RowCell {:value oms }]
   ))

(defn- transform-to-rows [selected-ids-store all-ids records]
    (transduce
     (map (fn [r] (TableRow selected-ids-store (count all-ids) r)))
     into [] records))


(defn TableBlock [patients-info selected-ids-store sorting current-page page-size]
    (fn [patients-info selected-ids-store sorting current-page page-size]
      (let [patients-info-records @patients-info
            all-ids (map :id patients-info-records)
            total (:total patients-info-records)]
        [:div
         [:p {:hidden false } @selected-ids-store]
         (-> [:div.patient-info-table-grid-container]
             (into (HeaderRow selected-ids-store all-ids sorting))
             (into (transform-to-rows selected-ids-store all-ids patients-info-records)))
         [Paging total current-page page-size]
        ;; [ButtonsLine !modal all-ids]
        ;; [PatientModal !modal]
         ]
        )))
