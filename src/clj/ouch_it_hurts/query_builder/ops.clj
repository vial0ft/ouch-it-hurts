(ns ouch-it-hurts.query-builder.ops)


(defn between [field from to]
  [field :between from :and to])


(defn _or
  ([] nil)
  ([single] single)
  ([first & seq] (reduce (fn [acc el] (conj acc :or el)) [first] seq)))


(defn _and
  ([] nil)
  ([single] single)
  ([first & seq] (reduce (fn [acc el] (conj acc :and el)) [first] seq)))

(defn eq [left right]
  [left := right])

(defn lt [left right]
  [left :< right])

(defn gt [left right]
  [left :> right])

(defn lte [left right]
  [left :<= right])

(defn gte [left right]
  [left :>= right])

(defn neq [left right]
  [left :<> right])




