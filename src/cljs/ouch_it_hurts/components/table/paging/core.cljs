(ns ouch-it-hurts.components.table.paging.core
  (:require [ouch-it-hurts.components.table.paging.items :refer [PageNumber SkippedNumber]]
            [reagent.core :as r]))


(def page-size-options [10 25 50])


(defn- seq-page-numbers [current max eps]
  (let [left-eps (- current eps)
        right-eps (+ current eps)
        before-part  (if (<= left-eps 1) (vec (range 1 current))
                         (->> (range left-eps current)
                             (vec)
                             (concat [1 :skip])
                             ))
        after-part (if (>= right-eps max) (vec (range current (inc max)))
                       (-> (range current (inc right-eps))
                           (vec)
                           (conj :skip max)))]
    (concat before-part after-part)
    ))

(defn- page-number-on-click [current-page-state]
  #(reset! current-page-state (.-id (.-target %))))


(defn- page-numbers [count-of-pages current-page-state]
  (let [numbers (seq-page-numbers @current-page-state count-of-pages 2)]
    (if (empty? numbers) [:span]
    (->> numbers
         (map #(if (= % :skip) [SkippedNumber]
                    [PageNumber {:key % :label (str %) :on-click (page-number-on-click current-page-state)}]))
    ))))


(defn PageSizeSelector [page-size-state options]
  (fn [page-size-state]
    (let [current-page-size @page-size-state]
      [:div {:style {:display "flex" :flex "nowrap" :width "5%"}}
       [:select {:default-value current-page-size}
        (for [size-option options]
          [:option {:value size-option :key size-option} size-option]
          )
        ]]
      )))

(defn Paging [total current-page-state page-size-state]
  (fn [total current-page-state page-size-state]
    (let [size            @page-size-state
          count-of-pages  (+ (unchecked-divide-int total size)
                             (if (pos-int? (mod total size)) 1 0))]
      [:div {:style {:display "flex"}}
       [:div {:style {:display "flex" :flex "nowrap" :justify-content "center" :width "90%"}}
         (page-numbers count-of-pages current-page-state)]
       [PageSizeSelector page-size-state page-size-options]]
    )))

