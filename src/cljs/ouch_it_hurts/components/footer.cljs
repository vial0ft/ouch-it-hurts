(ns ouch-it-hurts.components.footer)

(defn Footer []
  [:div.footer
   [:div
    [:a {:style {:text-decoration "none" :color "black"}
         :href "https://github.com/vial0ft/ouch-it-hurts"} "Source code"]]
   [:div
    "2023"]])
