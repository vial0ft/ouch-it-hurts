(ns ouch-it-hurts.components.table.core
  (:require
   [ouch-it-hurts.components.table.items :refer [OrderedHeaderCell RowCell]]
   [ouch-it-hurts.components.table.paging.core :refer [Paging]]
   [re-frame.core :as re :refer [subscribe]]))

(defn- key->label [k]
  (-> (name k)
      (clojure.string/replace  "-" " ")
      (clojure.string/capitalize)))

(defn- HeaderRow []
  (let [select-all-cell [:div.patients-info-table-header-grid-item
                         [:input {:key "all-select-checkbox"
                                  :type "checkbox"
                                  :checked @(subscribe [:table/all-rows-selected?])
                                  :on-change #(re/dispatch [:table/select-row :all (.-checked (.-target %))])}]]]
    (->> (for [h [:id :first-name :middle-name :last-name :sex :birth-date :address :oms]]
           [OrderedHeaderCell {:key h
                               :class "patients-info-table-header-grid-item"
                               :sorting (get @(subscribe [:sorting]) h)
                               :on-click #(re/dispatch [:table/sorting-change h %])} (key->label h)])
         (cons select-all-cell))))

(defn- TableRow [{:keys [id  deleted] :as row-info}]
  (let [class (if (:deleted row-info)  "patients-info-table-grid-deleted-item"
                  "patients-info-table-grid-item")
        select-cell
        ^{:key (str id "_select_row_key")} [RowCell {:class class}
                                            [:input {:type "checkbox"
                                                     :checked  @(subscribe [:table/selected? id])
                                                     :on-change #(re/dispatch [:table/select-row id (.-checked (.-target %))])}]]]
    (->> (for [rk [:id :first-name :middle-name :last-name :sex :birth-date :address :oms]]
           (let [class (if (:deleted row-info)  "patients-info-table-grid-deleted-item"
                           "patients-info-table-grid-item")]
             ^{:key (str rk "_row_key")} [RowCell {:class class} (get row-info rk nil)]))
         (cons select-cell))))

(defn- transform-to-rows [records] (mapv #(TableRow %) records))

(defn TableBlock [{:keys [patients sorting paging]}]
  (fn [{:keys [patients sorting paging]}]
    (let [{:keys [data total]} patients]
      [:div
       (-> [:div.patient-info-table-grid-container]
           (into (HeaderRow))
           (into (transform-to-rows data)))
      [Paging total paging]])))
