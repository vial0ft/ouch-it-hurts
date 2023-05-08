(ns ouch-it-hurts.utils.state
  (:require [reagent.core :as r]))

(defn use-state [state]
  (let [s (r/atom state)]
    [(fn
       ([] s)
       ([path] (r/cursor s path))) (fn [path value] (reset! (r/cursor s path) value))]))
