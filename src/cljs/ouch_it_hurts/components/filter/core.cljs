(ns ouch-it-hurts.components.filter.core
  (:require [reagent.core :as r]
            [ouch-it-hurts.components.common.core :refer [ErrorSpan]]
            [ouch-it-hurts.specs :as specs]
            [goog.string :as gstr]
            [clojure.string :refer [join]]
            [ouch-it-hurts.components.filter.left-block.core :refer [LeftBlock]]
            [ouch-it-hurts.components.filter.right-block.core :refer [RightBlock]]
            ))

;; -------------------------
;; States

(def ^private default-filter {:first-name {:value ""}
                              :last-name {:value ""}
                              :middle-name {:value ""}
                              :address {:value ""}
                              :birth-date-period {:value {}}
                              :sex-opts {:value #{}}
                              :oms {:value ""}
                              :show-records-opts {:value ""}})

(def ^private error-state (r/atom nil))

(def ^private filter-form (r/atom {:filters default-filter :error nil}))

(defn- filter-form-cursor
  ([] (r/cursor filter-form [:filters]))
  ([path] (r/cursor filter-form (into [:filters] path))))


(defn store-path-f [store]
  (fn
    ([] (r/cursor store [:filters]))
    ([path] (r/cursor store (into [:filters] path)))))

(def xform
  (comp
   (filter (fn [[k v]] (not (empty? (:value v)))))
   (map (fn [[k v]] {k (:value v)}))))

(defn- local-2-global [local-filters]
  (transduce
   xform
   into {}
   @local-filters))

(defn- on-click-clean [callback]
  (fn [_]
    (do
      (reset! filter-form  {:filters default-filter :error nil})
      (callback {}))))

(defn- on-submit-form [callback]
  (fn [e]
    (.preventDefault e)
    (let [[result details]
          (->> (local-2-global (filter-form-cursor))
               (specs/confirm-if-valid :ouch-it-hurts.specs/filters))]
      (case result
        :ok (do
              (reset! (r/cursor filter-form [:error]) nil)
              (callback details))
        (reset! (r/cursor filter-form [:error]) (join "\n" details))))))

(defn FilterForm [state-update-callback]
  (fn [state-update-callback]
    [:div {:style {:padding "10px"}}
     [:h2 "Filters"]
     [:form {:on-submit (on-submit-form state-update-callback)}
      [:div.filter-form {:name "filterForm"}
       [LeftBlock (store-path-f filter-form)]
       [RightBlock (store-path-f filter-form)]]
      [ErrorSpan @(r/cursor filter-form [:error])]
      [:div.filter-form-buttons-block
       [:button.filter-form-button {:on-click (on-click-clean state-update-callback)
                                    :type :reset} "Clear filters"]
       [:button.filter-form-button {:type :submit} "Apply filters"]]]]))

