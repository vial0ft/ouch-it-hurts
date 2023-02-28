(ns ouch-it-hurts.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [reagent.dom.client :as c])
  )


(enable-console-print!)


(defn simple-component []
  [:div
   [:p "Hello"]
   ])

(d/render [simple-component]
          (js/document.getElementById "main"))

