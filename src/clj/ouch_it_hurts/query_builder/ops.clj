(ns ouch-it-hurts.query-builder.ops)

(def supported-ops #{:between :is :in :or :and := :> :< :>= :<= :<> :like})

(def open (symbol "("))

(def close (symbol ")"))

(defn scoped [expr]
  (into [] (cons open (conj expr close))))

(defn is-null
  ([] [nil])
  ([key] (scoped [key :is :null])))

(defn _or
  ([] nil)
  ([single] [single])
  ([first & seq] (scoped (reduce (fn [acc el] (conj acc :or el)) [first] seq))))

(defn _and
  ([] nil)
  ([single] [single])
  ([first & seq] (scoped (reduce (fn [acc el] (conj acc :and el)) [first] seq))))

(defn eq [left right]
  (scoped [left := right]))

(defn lt [left right]
  (scoped [left :< right]))

(defn gt [left right]
  (scoped [left :> right]))

(defn lte [left right]
  (scoped [left :<= right]))

(defn gte [left right]
  (scoped [left :>= right]))

(defn neq [left right]
  (scoped [left :<> right]))

(defn _any
  ([] nil)
  ([key seq]
   (cond
     (some nil? seq) (let [without-nil  (set (filter some? seq))
                           count-not-nilables (count without-nil)]
                       (if (pos? count-not-nilables)
                         (->> (_any key without-nil) (_or (is-null key)))
                         (is-null key)))
     :absent-nil (scoped [key := (symbol "any(") (into-array seq) (symbol ")")]))))

(defn between
  ([field {:keys [from to]}] (between field from to))
  ([field from to]
   (case [(nil? from) (nil? to)]
     [true true] nil
     [true false] (lte field to)
     [false true] (gte field from)
     (scoped [field :between from :and to]))))

(defn like [field {:keys [pattern]}] (scoped [field :like pattern]))


(defn _count
  ([] (_count [:*]))
  ([columns] (into [] (cons (symbol "count(") (conj columns close)))))
