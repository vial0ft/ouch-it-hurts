(ns client.front-dev
  (:require
   [reagent.core :as r]
   [reagent.dom :as d]
   [reagent.dom.client :as c])
  )


(enable-console-print!)


(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(d/render [simple-component]
          (js/document.getElementById "main"))



