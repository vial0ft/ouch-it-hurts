(ns ouch-it-hurts.components.filter.left-block.core
  (:require
   [ouch-it-hurts.components.common.core :refer [FieldSet LabledField SingleFieldSet Select ErrorSpan]]
   [ouch-it-hurts.components.filter.items :refer [CheckboxButton]]
   [ouch-it-hurts.specs :as specs]
   [re-frame.core :as re :refer [subscribe]]))

(def all-sex-options specs/sex-filter-opts)

(defn- patient-name-filter-block []
  [FieldSet "Patient name"
   (for [[key text] [[:first-name "First name: "]
                     [:middle-name "Middle name: "]
                     [:last-name "Last name: "]]]
     ^{:key (str (name key) "_filter_name" )}
     [LabledField {:key (name key)
                   :class "filter-form-block-item"
                   :lable {:class "filter-form-block-item-lable" :text text}
                   :input {:class "filter-form-block-item-text-input"
                           :type "text"
                           :on-change #(re/dispatch [:filters/change key %])}}])])

(defn- patient-sex-filter-selector [store-path-by]
  [FieldSet "Sex options"
   [:div.sex-filed-set {:key "filter_sex_opt_block"}
    (let [opts (->> (for [so all-sex-options]
                      {:key so
                       :checked @(subscribe [:filters/sex-opts-selected? so])
                       :on-change #(re/dispatch [:filters/sex-opts so (.-checked (.-target %))])
                       :label (clojure.string/capitalize so)})
                    (cons {:key "all"
                           :checked @(subscribe [:filters/all-sex-opts-selected?])
                           :on-change #(if (.-checked (.-target %)) (re/dispatch [:filters/sex-opts :all true])
                                           (do (.preventDefault %) (.stopPropagation %) true))
                           :label "All"}))]
      (for [o (into [] opts)]
        ^{:key (str (:key o) "_filter_sex_opt")}
        [CheckboxButton {:key (:key o) :opt (select-keys o [:on-change :checked])}  (:label o)]))]])

(defn- patient-show-options [store-path-by]
  [FieldSet "Show record options"
   [Select {:key "show-record-options"
            :options (let [default :not-deleted-only
                           all [{:value :not-deleted-only :lable "Not deleted only"}
                                {:value :deleted-only :lable "Deleted only"}
                                {:value :all :lable "All"}]]
                       (map #(if (= (:value %) default) (assoc % :selected true) %) all))
            :on-change #(re/dispatch [:filters/change :show-records-opts %])}]])

(defn LeftBlock []
  [:div.filter-form-block
   [patient-name-filter-block]
   [patient-sex-filter-selector]
   [patient-show-options]])
