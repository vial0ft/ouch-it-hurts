(ns ouch-it-hurts.components.filter.core
  (:require [reagent.core :as r]
            [re-frame.core :as re :refer [subscribe]]
            [ouch-it-hurts.components.common.core :refer [ErrorSpan]]
            [ouch-it-hurts.specs :as specs]
            [goog.string :as gstr]
            [clojure.string :refer [join]]
            [ouch-it-hurts.components.filter.left-block.core :refer [LeftBlock]]
            [ouch-it-hurts.components.filter.right-block.core :refer [RightBlock]]))

(defn- on-submit-form []
  (fn [e]
    (.preventDefault e)
    (let [[result details] (specs/confirm-if-valid :ouch-it-hurts.specs/filters @(subscribe [:filters]))]
      (case result
        :ok (re/dispatch [:table/paging-change {:page-number 1}])
        (re/dispatch [:filters/error (join "\n" details)])))))

(defn FilterForm [filters]
  (fn [filters]
    [:div {:style {:padding "10px"}}
     [:h2 "Filters"]
     [:form {:on-submit (on-submit-form)}
      [:div.filter-form {:name "filterForm"}
       [LeftBlock]
       [RightBlock]]
      [ErrorSpan @(subscribe [:filter-error])]
      [:div.filter-form-buttons-block
       [:button.filter-form-button {:on-click #(re/dispatch [:filters/clean])
                                    :type :reset} "Clear filters"]
       [:button.filter-form-button {:type :submit} "Apply filters"]]]]))

