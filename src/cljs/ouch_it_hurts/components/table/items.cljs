(ns ouch-it-hurts.components.table.items)

(def ^:private order-map {"↕" nil
                          "↑" :desc
                          "↓" :asc})

(defn- sorting->order-symbol [s]
  (get (clojure.set/map-invert order-map) s nil))

(defn- shift-order-value [curr]
  (case curr
    "↕" "↓"
    "↓" "↑"
    "↕"))

(defn OrderedHeaderCell [{:keys [key class sorting on-click]} value]
  (fn [{:keys [class sorting on-click]}]
    (let [order-value (sorting->order-symbol sorting)]
      [:div {:key key
             :class class
             :on-click #(on-click (->> order-value
                                      shift-order-value
                                      (get order-map)))}
       [:span value]
       [:span {:class "spaced"} order-value]])))

(defn RowCell [{:keys [class]} value]
  [:div {:class class} value])
