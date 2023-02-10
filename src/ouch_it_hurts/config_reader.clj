(ns ouch-it-hurts.config-reader
  (:require
   [clojure.java.io :as io]))


(def ^:const system-filename "system.edn")

(defn get-env
  ([key] (get-env key nil))
  ([key default] (or (get (System/getenv) key) default)))

(defn resolve-props [props]
  (let [{:keys [env default]} props]
    (cond
      (not (nil? env)) (get-env env default)
      (and (nil? env) (not (nil? default))) default
      :else (if (map? props) (update-vals props resolve-props) props))))

(defn read-config
  ([] (read-config system-filename))
  ([config-filename] (read-string (slurp (io/resource config-filename))))
  ([config-filename keys] (select-keys (read-config config-filename) keys)))
