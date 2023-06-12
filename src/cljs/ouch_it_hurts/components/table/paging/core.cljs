(ns ouch-it-hurts.components.table.paging.core
  (:require [ouch-it-hurts.components.table.paging.items :refer [PageNumber SkippedNumber PageSizeSelector]]
            [reagent.core :as r]
            [re-frame.core :as re :refer [subscribe]]))

(def page-size-options [10 25 50])

(defn- count-pages [total page-size]
  (+ (quot total page-size) (if (pos-int? (mod total page-size)) 1 0)))

(defn- seq-page-numbers [current max eps]
  (let [left-eps (- current eps)
        right-eps (+ current eps)
        before-part  (if (<= left-eps 1) (vec (range 1 current))
                         (->> (range left-eps current)
                              (vec)
                              (concat [1 :skip-l])))
        after-part (if (>= right-eps max) (vec (range current (inc max)))
                       (-> (range current (inc right-eps))
                           (vec)
                           (conj :skip-r max)))]
    (concat before-part after-part)))

(defn Paging [total paging]
  (fn [total paging]
    (let [{:keys [page-number page-size]} paging
          count-of-pages  (count-pages total page-size)
          seq-numbers (seq-page-numbers page-number count-of-pages 2)]
      [:div.paging-line
       [:div {:style {:display "flex" :flex "nowrap" :justify-content "center" :width "90%"}}
        (if (empty? seq-numbers) [:span]
            (for [number seq-numbers]
              (case number
                :skip-l ^{:key number} [SkippedNumber]
                :skip-r ^{:key number} [SkippedNumber]
                (let [key {:key (str number "_page_number")}
                      attrs key
                      additional-attrs (if (= number page-number)
                                         (merge attrs {:id "paging-current-number-button"})
                                         (merge attrs {:id "paging-number-button"
                                                       :opt {:on-click #(re/dispatch [:table/paging-change {:page-size page-size
                                                                                                            :page-number number}])}}))]
                  ^{:key key} [PageNumber additional-attrs (str number)]))))]
       [PageSizeSelector
        {:current page-size
         :options page-size-options
         :on-change #(re/dispatch [:table/paging-change {:page-size (js/parseInt %)
                                                         :page-number 1}])}]])))
