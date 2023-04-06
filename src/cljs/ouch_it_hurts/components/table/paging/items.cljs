(ns ouch-it-hurts.components.table.paging.items)





(defn PageNumber [{:keys [key label opt]}]
  [:div {:id "paging-number-button"}
   [:label {:for key}
    [:input  (merge
              {:id key
               :name key
               :type "checkbox"}
              opt)]
    [:span label]
    ]])


(defn SkippedNumber []
  [:div {:id "paging-skipped-number-button"}
   [:label [:span "..."]]])
