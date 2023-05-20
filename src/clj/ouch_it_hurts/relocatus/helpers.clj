(ns ouch-it-hurts.relocatus.helpers
  (:require [clojure.java.io :as io]
            [clojure.string :as s]))

(defn schema-table [{:keys [schema table]}]
  (str schema "." table))

(defn hash-of-pair [p s] (hash-ordered-coll [p s]))

(defn create-files [dir file-names]
  (let [dir-path (-> (io/resource dir) (io/as-file) (.getPath))]
    (doseq [file-name  file-names]
      (-> (java.nio.file.Paths/get dir-path (into-array [file-name]))
          (.toFile)
          (.createNewFile)))))

(defn- look-up-dir [dir]
  (-> (io/resource dir)
    	(.getFile)
      (java.io.File.)
      (file-seq)))

(defn migration-scripts-names [dir]
  (->> (look-up-dir dir)
       (filterv #(s/ends-with? % ".sql"))))

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
    (->> (look-up-dir migrations-dir)
         (filter #(re-find pattern (.getName %)))
         (up-down-map))))
