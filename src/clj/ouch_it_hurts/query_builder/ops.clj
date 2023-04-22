(ns ouch-it-hurts.query-builder.ops)


(def supported-ops #{:between :is :in :or :and := :> :< :>= :<= :<>})

(defn between [field from to]
  [field :between from :and to])

(defn is-null
  ([] nil)
  ([key] [key :is :null]))

(defn _or
  ([] nil)
  ([single] [single])
  ([first & seq] (reduce (fn [acc el] (conj acc :or el)) [first] seq)))


(defn _and
  ([] nil)
  ([single] [single])
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

(defn in
  ([] nil)
  ([key seq]
   (cond
     (some nil? seq) (let [without-nil  (set (filter some? seq))
                           count-not-nilables (count without-nil)]
                       (if (pos? count-not-nilables)
                         (->> (in key without-nil) (_or (is-null key)))
                         (is-null key)))
     :absent-nil [key :in seq])))
