(ns ouch-it-hurts.relocatus.helpers
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defn schema-table [{:keys [schema table]}]
  (str schema "." table))

(defn hash-of-pair [p s] (hash-ordered-coll [p s]))

(defn create-files [dir file-names]
  (let [dir (io/as-file (io/resource dir))
        _ (when-not (.exists dir) (.mkdirs dir))
        dir-path (.getPath dir)]
    (doseq [file-name  file-names]
      (-> (java.nio.file.Paths/get dir-path (into-array [file-name]))
          (.toFile)
          (.createNewFile)))))

(defn migration-scripts-names [dir]
  (->> (io/resource dir) (io/as-file) (.listFiles) (vec)))

(defn up-down-map [up-and-down]
  {:up (some #(if (s/ends-with? % "up.sql") %) up-and-down)
   :down (some #(if (s/ends-with? % "down.sql") %) up-and-down)})

(defn parse-migration-name [file]
  (->> (.getName file)
       (re-find #"(.*[0-9]_.*)\..*\.sql")
       (second)))

(defn migration-name-without-time [migration-name]
  (s/replace migration-name #"^.*\d_" ""))

(defn migration-scripts-map [dir]
  (->> (migration-scripts-names dir)
       (group-by parse-migration-name)
       (map (fn [[k v]] [k (up-down-map v)]))
       (into (sorted-map))))

(defn up-down-migration-scripts [migrations-dir migration-name]
  (let [pattern (re-pattern (format "^.*[0-9]_%s\\.(?:up|down)\\.sql" migration-name))]
    (->> (io/resource migrations-dir)
         (io/as-file)
         (file-seq)
         (filter #(re-find pattern (.getName %)))
         (up-down-map))))
