(ns ouch-it-hurts.components.filter.filter-form
  (:require [reagent.core :as r]
            [ouch-it-hurts.utils.datetime-utils :as dtu]
            [ouch-it-hurts.components.filter.filter-items :refer [LabledField DateRangeField SingleFieldSet FieldSet CheckboxButton]]))


;; -------------------------
;; States


(def ^private default-filter {
                              :id ""
                              :first-name ""
                              :second-name ""
                              :middle-name ""
                              :address ""
                              :birth-date {
                                           :from nil
                                           :to nil}
                              :sex #{}
                              :oms ""
                              })

(def all-sex-options #{"all" "male" "female" "other"})

(def ^private filter-form (r/atom default-filter))

(defn- filter-form-cursor [path]
  (r/cursor filter-form path))


(defn- change-key [key-path]
  (fn [e]
    (reset! (filter-form-cursor key-path) (.-value (.-target e)))))


(defn- choose-all-selected [selector-id]
  (let [selector (js/document.getElementById selector-id)
        selected-options (.. selector -selectedOptions)
        values (-> (.from js/Array selected-options)
                   (.map #(.. % -value))
                   (js->clj))]
    values
    ))

(defn- clean-selected [selector-id]
  (let [selector (js/document.getElementById selector-id)
        selected-options (.. selector -selectedOptions)]
    (-> (.from js/Array selected-options)
        (.forEach #(set! (.. % -selected) false)))
    ))

(defn- select-filter-item [{:keys [item-key mutable on-change options]}]
   [:select {:id item-key
             :name item-key
             :multiple mutable
             :on-change on-change}
   (for [opt options]
     [:option (second opt) (first opt)]
     )])

(defn- filter-clean-button []
  [:button.filter-form-button
   {:on-click #(reset! filter-form default-filter)
    :type :reset}
   "Clear filters"
   ]
  )

(defn- filter-apply-button [filter-callback]
  [:button.filter-form-button
   {:on-click #(filter-callback @filter-form)
    :type :submit}
   "Apply filters"
   ])

(defn- patient-name-filter-block []
  [FieldSet "Patient name"
   [LabledField {:key "first-name"
                 :label-text "First name: "
                 :input-type "text"
                 :on-change (change-key [:first-name])}]
   [LabledField {:key "middle-name"
                 :label-text "Middle name: "
                 :input-type "text"
                 :on-change (change-key [:middle-name])}]
   [LabledField {:key "second-name"
                 :label-text "Second name: "
                 :input-type "text"
                 :on-change (change-key [:second-name])}]
   ])


(defn- update-filter [path]
  (fn [e] (r/swap! filter-form assoc-in path )))


(defn- patient-sex-filter-selector []
  [FieldSet "Sex"
   [CheckboxButton {:key "all"
                    :label "All"
                    :opt {
                          :defaultChecked true
                          :on-change #(println (.-checked (.-target %)))
                          }}]
   [CheckboxButton {:key "male"
                    :label "Male"
                    :on-click #(js/alert %)}]
   [CheckboxButton {:key "female"
                    :label "Female"
                    :on-click #(js/alert %)}]
   [CheckboxButton {:key "other"
                    :label "Other"
                    :on-click #(js/alert %)}]
   [CheckboxButton {:key "none"
                    :label "None"
                    :on-click #(js/alert %)}]


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
     :on-change (change-key [:address])
     }]
   [SingleFieldSet
    {:legend "CMI number"
     :key "cmi"
     :input-type "text"
     :on-change (change-key [:oms])
     }]
   [DateRangeField
    {:legend "Birth date"
     :key "birth-date"
     :on-change-from (change-key [:birth-date :from])
     :on-change-to (change-key [:birth-date :to])}]
  ])





(defn FilterForm [callback]
  [:div {:style {:padding "10px"}}
   [:p {:hidden false} (str @filter-form)]
   [:form {:on-submit (fn [e]
                        (println e)
                        (println  (.preventDefault e))
                        )}
    [:div.filter-form
     [patient-left-filter-block]
     [patient-right-filter-block]]
    [:div.filter-form-buttons-block
     [filter-clean-button]
     [filter-apply-button callback]
     ]
   ]])


(comment

)
