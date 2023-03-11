(ns ouch-it-hurts.components.filter.filter-form
  (:require [reagent.core :as r]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [ouch-it-hurts.components.filter.filter-items :refer [LabledField DateRangeField SingleFieldSet FieldSet CheckboxButton]]))


;; -------------------------
;; States


(def ^private default-filter {
                              :first-name ""
                              :second-name ""
                              :middle-name ""
                              :address ""
                              :birth-date {:error {:error? false :message ""}}
                              :sex #{}
                              :oms {:error {:error? false :message ""}}
                              })

(def all-sex-options #{"male" "female" "other" "none"})

(def ^private filter-form (r/atom default-filter))

(defn- filter-form-cursor [path]
  (r/cursor filter-form path))


(defn- change-key [key-path]
  (fn [new-value] (reset! key-path new-value)))

(defn- filter-clean-button []
  [:button.filter-form-button
   {
    :on-click #(reset! filter-form default-filter)
    :type :reset}
   "Clear filters"
   ]
  )

(defn- filter-apply-button [filter-callback]
  [:button.filter-form-button
   {
   ;; :on-click #(filter-callback @filter-form)
    :type :submit
    }
   "Apply filters"
   ])

(defn- patient-name-filter-block []
  [FieldSet "Patient name"
   [LabledField {:key "first-name"
                 :label-text "First name: "
                 :input-type "text"
                 :on-change (change-key (filter-form-cursor [:first-name]))}]
   [LabledField {:key "middle-name"
                 :label-text "Middle name: "
                 :input-type "text"
                 :on-change (change-key (filter-form-cursor [:middle-name]))}]
   [LabledField {:key "second-name"
                 :label-text "Second name: "
                 :input-type "text"
                 :on-change (change-key (filter-form-cursor [:second-name]))}]
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
                       (reset! (filter-form-cursor [:sex]) #{}))
        (if checked? (do
                       (set-elems-value {"all" false id true})
                       (swap! (filter-form-cursor [:sex]) conj id))
                       ))
      )))

(defn- patient-sex-filter-selector []
  [FieldSet "Sex"
   [CheckboxButton {:key "all"
                    :label "All"
                    :opt {:defaultChecked true
                          :on-change (sex-filter-on-change all-sex-options)}}]
   [CheckboxButton {:key "male"
                    :label "Male"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}}]
   [CheckboxButton {:key "female"
                    :label "Female"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}}]
   [CheckboxButton {:key "other"
                    :label "Other"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}}]
   [CheckboxButton {:key "none"
                    :label "None"
                    :opt {:on-change (sex-filter-on-change all-sex-options)}}]
   ]
  )



(defn- patient-left-filter-block []
  [:div.filter-form-block
   [patient-name-filter-block]
   [patient-sex-filter-selector]]
   )

(defn- patient-right-filter-block []
  [:div.filter-form-block
   [SingleFieldSet
    {:legend "Address"
     :key "address"
     :input-type "text"
     :on-change (change-key (filter-form-cursor [:address]))
     }]
   [SingleFieldSet
    {:legend "CMI number"
     :key "cmi"
     :input-type "text"
     :on-change (change-key (filter-form-cursor [:oms :value]))
     :error @(filter-form-cursor [:oms :error])
     }]
   [DateRangeField
    {:legend "Birth date"
     :key "birth-date"
     :from {:on-change (change-key (filter-form-cursor [:birth-date :from]))}
     :to {:on-change (change-key (filter-form-cursor [:birth-date :to]))}
     :error @(filter-form-cursor [:birth-date :error])
   }]])


(defn FilterForm [callback]
  [:div {:style {:padding "10px"}}
   [:p {:hidden false} (str @filter-form)]
   [:form {:on-submit (fn [e]
                        (callback @filter-form)
                        (println "submit")
                        (.preventDefault e))} ;; TODO add validation before callback
    [:div.filter-form {:name "filterForm"}
     [patient-left-filter-block]
     [patient-right-filter-block]]
    [:div.filter-form-buttons-block
     [filter-clean-button]
     [filter-apply-button callback]
     ]
   ]])


(comment

  (def test-atom (r/atom #{1}))

  (swap! test-atom (fn [old]  (println old ) old))


  (conj #{1} 2)
)
