(ns ouch-it-hurts.components.filter.left-block.core
  (:require
   [ouch-it-hurts.components.common.core :refer [FieldSet LabledField SingleFieldSet Select ErrorSpan]]
   [ouch-it-hurts.components.filter.items :refer [CheckboxButton]]
   [goog.string :as gstr]
   [ouch-it-hurts.specs :as specs]))

(def all-sex-options specs/sex-filter-opts)

(defn- change-key [key-path] (fn [new-value] (reset! key-path new-value)))

(defn- patient-name-filter-block [store-path-by]
  [FieldSet "Patient name"
   (for [[key text] [[:first-name "First name: "]
                     [:middle-name "Middle name: "]
                     [:last-name "Last name: "]]]
     ^{:key (gstr/format "%s__filter_name" (name key))}
     [LabledField {:key (name key)
                   :class "filter-form-block-item"
                   :lable {:class "filter-form-block-item-lable" :text text}
                   :input {:class "filter-form-block-item-text-input"
                           :type "text"
                           :on-change (change-key (store-path-by [key :value]))}}])])

(defn- set-elems-value [id-value-pairs]
  (doseq [[id value]  id-value-pairs]
    (when-let [elem (js/document.getElementById id)]
      (set! (.-checked elem) value))))

(defn- sex-filter-on-change [store-path-by sex-keys]
  (fn [e]
    (let [checked? (.-checked (.-target e))
          id (.-id (.-target e))]
      (case [id checked?]
        ["all" true] (do
                       (set-elems-value (into {"all" true} (map (fn [s] [s false]) sex-keys)))
                       (reset! (store-path-by [:sex-opts :value]) #{}))
        (if checked?
          (do
            (set-elems-value {"all" false id true})
            (swap! (store-path-by [:sex-opts :value]) conj id))
          (do
            (swap! (store-path-by [:sex-opts :value]) (fn [old] (disj old id)))
            (when (empty? @(store-path-by [:sex-opts :value])) (set-elems-value {"all" true}))))))))

(defn- patient-sex-filter-selector [store-path-by]
  [FieldSet "Sex options"
   [:div.sex-filed-set {:key "filter_sex_opt_block"}
    (let [opts (->> (for [so all-sex-options]
                      {:key so
                       :on-change (sex-filter-on-change store-path-by  all-sex-options)
                       :label (gstr/toTitleCase so)})
                    (cons {:key "all"
                           :on-change (sex-filter-on-change store-path-by all-sex-options)
                           :label "All"
                           :defaultChecked true}))]
      (for [o opts]
        ^{:key (gstr/format "%s__filter_sex_opt" (:key o))}
        [CheckboxButton {:key (:key o) :opt (select-keys o [:on-change :defaultChecked])}  (:label o)]))]])

(defn- patient-show-options [store-path-by]
  [FieldSet "Show record options"
   [Select {:key "show-record-options"
            :options (let [default :not-deleted-only
                           all [{:value :not-deleted-only :lable "Not deleted only"}
                                {:value :deleted-only :lable "Deleted only"}
                                {:value :all :lable "All"}]]
                       (map #(if (= (:value %) default) (assoc % :selected true) %) all))
            :on-change #(reset! (store-path-by [:show-records-opts :value])  %)}]])

(defn LeftBlock [store-path-f]
  [:div.filter-form-block
   [patient-name-filter-block store-path-f]
   [patient-sex-filter-selector store-path-f]
   [patient-show-options store-path-f]])
