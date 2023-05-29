(ns ouch-it-hurts.components.filter.core
  (:require [reagent.core :as r]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [ouch-it-hurts.components.filter.items :refer [DateRangeField CheckboxButton]]
            [ouch-it-hurts.components.common.core :refer [FieldSet LabledField SingleFieldSet Select ErrorSpan]]
            [ouch-it-hurts.specs :as specs]
            [clojure.string :refer [join]]))

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

(def all-sex-options #{"male" "female" "unknown"})

(def ^private filter-form (r/atom {:filters default-filter
                                   :error nil}))

(defn- filter-form-cursor
  ([] (r/cursor filter-form [:filters]))
  ([path] (r/cursor filter-form (into [:filters] path))))

(defn- change-key [key-path]
  (fn [new-value] (reset! key-path new-value)))

(defn- filter-clean-button [on-click]
  [:button.filter-form-button
   {:on-click on-click
    :type :reset}
   "Clear filters"])

(defn- filter-apply-button []
  [:button.filter-form-button
   {:type :submit}
   "Apply filters"])

(defn- patient-name-filter-block []
  [FieldSet "Patient name"
   [LabledField {:key "first-name"
                 :class "filter-form-block-item"
                 :lable {:class "filter-form-block-item-lable" :text "First name: "}
                 :input {:class "filter-form-block-item-text-input"
                         :type "text"
                         :on-change (change-key (filter-form-cursor [:first-name :value]))}}]
   [LabledField {:key "middle-name"
                 :class "filter-form-block-item"
                 :lable {:class "filter-form-block-item-lable" :text "Middle name: "}
                 :input {:class "filter-form-block-item-text-input"
                         :type "text"
                         :on-change (change-key (filter-form-cursor [:middle-name :value]))}}]
   [LabledField {:key "last-name"
                 :class "filter-form-block-item"
                 :lable {:class "filter-form-block-item-lable" :text "Last name: "}
                 :input {:class "filter-form-block-item-text-input"
                         :type "text"
                         :on-change (change-key (filter-form-cursor [:last-name :value]))}}]])

(defn- set-elems-value [id-value-pairs]
  (doseq [[id value]  id-value-pairs]
    (when-let [elem (js/document.getElementById id)]
      (set! (.-checked elem) value))))

(defn- sex-filter-on-change [sex-keys]
  (fn [e]
    (let [checked? (.-checked (.-target e))
          id (.-id (.-target e))]
      (println id checked?)
      (case [id checked?]
        ["all" true] (do
                       (set-elems-value (into {"all" true} (map (fn [s] [s false]) sex-keys)))
                       (reset! (filter-form-cursor [:sex-opts :value]) #{}))
        (if checked?
          (do
            (set-elems-value {"all" false id true})
            (swap! (filter-form-cursor [:sex-opts :value]) conj id))
          (do
            (swap! (filter-form-cursor [:sex-opts :value]) (fn [old] (disj old id)))
            (when (empty? @(filter-form-cursor [:sex-opts :value])) (set-elems-value {"all" true}))
                 ))))))

(defn- patient-sex-filter-selector []
  [FieldSet "Sex"
   [:div.sex-filed-set
   [CheckboxButton {:key "all"
                    :opt {:defaultChecked true
                          :on-change (sex-filter-on-change all-sex-options)}}  "All"]
   [CheckboxButton {:key "male"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Male"]
   [CheckboxButton {:key "female"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Female"]
   [CheckboxButton {:key "unknown"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Undefined"]]])

(defn patient-show-options []
  [FieldSet "Show record options"
   [Select {:key "show-record-options"
            :options (let [default :all
                           all [{:value :not-deleted-only :lable "Not deleted only"}
                                {:value :deleted-only :lable "Deleted only"}
                                {:value :all :lable "All"}]]
                       (map #(if (= (:value %) default) (assoc % :selected true) %) all))
            :on-change #(reset! (filter-form-cursor [:show-records-opts :value])  %)}]])

(defn- patient-left-filter-block []
  [:div.filter-form-block
   [patient-name-filter-block]
   [patient-sex-filter-selector]
   [patient-show-options]])

(defn- patient-right-filter-block []
  [:div.filter-form-block
   [SingleFieldSet
    {:key "address"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (filter-form-cursor [:address :value]))}}
    "Address"]
   [SingleFieldSet
    {:key "oms"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (filter-form-cursor [:oms :value]))}}
    "CMI number"]
   [DateRangeField
    {:key "birth-date"
     :from {:on-change (change-key (filter-form-cursor [:birth-date-period :value :from]))}
     :to {:on-change (change-key (filter-form-cursor [:birth-date-period :value :to]))}}
    "Birth date"]])

(def xform
  (comp
   (filter (fn [[k v]] (not (empty? (:value v)))))
   (map (fn [[k v]] {k (:value v)}))))

(defn- local-2-global [local-filters]
  (transduce
   xform
   into {}
   @local-filters))

(defn- on-click-clean-button [filter-state-update-callback]
  (fn [_]
    (do
      (reset! filter-form  {:filters default-filter :error nil})
      (filter-state-update-callback {}))))

(defn FilterForm [filter-state-update-callback]
  (fn [filter-state-update-callback]
    [:div {:style {:padding "10px"}}
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (let [[result details]
                                (->> (local-2-global (filter-form-cursor))
                                     (specs/confirm-if-valid :ouch-it-hurts.specs/filters))]
                            (case result
                              :ok (do
                                    (reset! (r/cursor filter-form [:error]) nil)
                                    (filter-state-update-callback details))
                              (reset! (r/cursor filter-form [:error]) (join "\n" details)))))}
      [:div.filter-form {:name "filterForm"}
       [patient-left-filter-block]
       [patient-right-filter-block]]
      [ErrorSpan @(r/cursor filter-form [:error])]
      [:div.filter-form-buttons-block
       [filter-clean-button (on-click-clean-button filter-state-update-callback)]
       [filter-apply-button]]]]))

