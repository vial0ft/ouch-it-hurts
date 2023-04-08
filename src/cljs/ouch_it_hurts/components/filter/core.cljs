(ns ouch-it-hurts.components.filter.core
  (:require [reagent.core :as r]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [ouch-it-hurts.components.filter.items :refer [DateRangeField CheckboxButton]]
            [ouch-it-hurts.components.common.core :refer [FieldSet LabledField SingleFieldSet Select]]))


;; -------------------------
;; States


(def ^private default-filter {
                              :first-name {:value ""}
                              :second-name {:value ""}
                              :middle-name {:value ""}
                              :address {:value ""}
                              :show-records {:value ""}
                              :birth-date {:value {} :error {:error? false :message ""}}
                              :sex {:value #{}}
                              :oms {:value "" :error {:error? false :message ""}}
                              })

(def all-sex-options #{"male" "female" "other" "none"})

(def ^private filter-form (r/atom default-filter))

(defn- filter-form-cursor [path]
  (r/cursor filter-form path))


(defn- change-key [key-path]
  (fn [new-value] (reset! key-path new-value)))

(defn- filter-clean-button [on-click]
  [:button.filter-form-button
   {
    :on-click on-click
    :type :reset}
   "Clear filters"
   ]
  )

(defn- filter-apply-button []
  [:button.filter-form-button
   {
    :type :submit
    }
   "Apply filters"
   ])

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
   [LabledField {:key "second-name"
                 :class "filter-form-block-item"
                 :lable {:class "filter-form-block-item-lable" :text "Second name: "}
                 :input {:class "filter-form-block-item-text-input"
                         :type "text"
                         :on-change (change-key (filter-form-cursor [:second-name :value]))}}]
   ])


(defn- set-elems-value [id-value-pairs]
  (doseq [[id value]  id-value-pairs]
    (when-let [elem (js/document.getElementById id)]
      (set! (.-checked elem) value)
      )
    ))

(defn- sex-filter-on-change [sex-keys]
  (fn [e]
    (let [checked? (.-checked (.-target e))
          id (.-id (.-target e))]
      (case [id checked?]
        ["all" true] (do
                       (set-elems-value (into {"all" true} (map (fn [s] [s false]) sex-keys)))
                       (reset! (filter-form-cursor [:sex :value]) #{}))
        (if checked?
          (do
            (set-elems-value {"all" false id true})
            (swap! (filter-form-cursor [:sex :value]) conj id))
          (swap! (filter-form-cursor [:sex :value]) (fn [old]
                                               (into (hash-set) (filter #(not= % id) old))))
                       ))
      )))

(defn- patient-sex-filter-selector []
  [FieldSet "Sex"
   [CheckboxButton {:key "all"
                    :opt {:defaultChecked true
                          :on-change (sex-filter-on-change all-sex-options)}}  "All" ]
   [CheckboxButton {:key "male"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Male"]
   [CheckboxButton {:key "female"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Female"]
   [CheckboxButton {:key "other"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "Other"]
   [CheckboxButton {:key "none"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}} "None"]
   ]
  )

(defn patient-show-options []
  [FieldSet "Show record options"
   [Select {:key "show-record-options"
            :options (let [default :not-deleted-only
                           all [{:value :not-deleted-only :lable "Not deleted only"}
                                {:value :deleted-only :lable "Deleted only"}
                                {:value :all :lable "All"}]]
                       (map #(if (not= (:value %) default) % (assoc % :default true)) all))
            :on-change #(reset! (filter-form-cursor [:show-records :value])  %)}]
   ]
  )



(defn- patient-left-filter-block []
  [:div.filter-form-block
   [patient-name-filter-block]
   [patient-sex-filter-selector]
   [patient-show-options]]
   )

(defn- patient-right-filter-block []
  [:div.filter-form-block
   [SingleFieldSet
    {:key "address"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (filter-form-cursor [:address :value]))
             }}
    "Address"]
   [SingleFieldSet
    {:key "cmi"
     :input {:type "text"
             :style {:width "100%"}
             :on-change (change-key (filter-form-cursor [:oms :value]))}
     :error @(filter-form-cursor [:oms :error])}
    "CMI number"]
   [DateRangeField
    {:key "birth-date"
     :from {:on-change (change-key (filter-form-cursor [:birth-date :value :from]))}
     :to {:on-change (change-key (filter-form-cursor [:birth-date :value :to]))}
     :error @(filter-form-cursor [:birth-date :error])}
    "Birth date"]])


(def xform
  (comp
   (filter (fn[[k v]] (not (empty? (:value v)))))
   (map (fn [[k v]] {k (:value v)}))
   )
  )

(defn- local-2-global [local-filters]
  (transduce
   xform
   into {}
   @local-filters))

(defn- on-click-clean-button [app-filter]
  (fn [_]
    (reset! app-filter {})
    (reset! filter-form default-filter)
    ))


(defn FilterForm [filter-state]
  (fn [filter-state]
    [:div {:style {:padding "10px"}}
     [:p {:hidden false} (str @filter-form)]
     [:form {:on-submit (fn [e]
                          (reset! filter-state (local-2-global filter-form))
                          (println "submit")
                          (.preventDefault e))} ;; TODO add validation before callback
      [:div.filter-form {:name "filterForm"}
       [patient-left-filter-block]
       [patient-right-filter-block]]
      [:div.filter-form-buttons-block
       [filter-clean-button (on-click-clean-button filter-state)]
       [filter-apply-button]
       ]
      ]]))

