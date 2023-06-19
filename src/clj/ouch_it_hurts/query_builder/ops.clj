(ns ouch-it-hurts.query-builder.ops)

(defn _or
  ([] nil)
  ([single] [single])
  ([first seq] (reduce (fn [acc el] (conj acc el)) [:and] (into [] seq))))

(defn _and
  ([] nil)
  ([single] [single])
  ([first seq] (reduce (fn [acc el] (conj acc el)) [:and] (into [] seq))))

(defn is-null
  ([] [nil])
  ([key] [:is key nil]))

(defn eq [left right]
  [:= left right])

(defn lt [left right]
  [:< left right])

(defn gt [left right]
  [:> left right])

(defn lte [left right]
  [:<= left right])

(defn gte [left right]
  [:>= left right])

(defn neq [left right]
  [:<> left right])

(defn _in
  [key seq]
  (cond
    (some nil? seq) (let [without-nil  (set (filter some? seq))
                          count-not-nilables (count without-nil)]
                      (if (pos? count-not-nilables)
                        [:or (_in key without-nil) (is-null key)]
                        (is-null key)))
    :absent-nil [:in key seq]))

(defn between
  ([field {:keys [from to]}] (between field from to))
  ([field from to]
   (case [(nil? from) (nil? to)]
     [true true] nil
     [true false] (lte field to)
     [false true] (gte field from)
     [:between field from to])))

(defn like [field {:keys [pattern]}] [:like field pattern])

