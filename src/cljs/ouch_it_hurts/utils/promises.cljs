(ns ouch-it-hurts.utils.promises)

(defn retry
  ([f count] (cond
               (<= count 0) f
               (= count 1) (f)
               :else (-> (f) (.catch #(retry f (dec count)))))))
