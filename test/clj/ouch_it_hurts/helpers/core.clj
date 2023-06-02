(ns ouch-it-hurts.helpers.core
  (:require [clj-test-containers.core :as tc]
            [clojure.java.io :as io]))

(defn create-container [{:keys [dbname user password]}]
  (tc/create {:image-name    "postgres:15rc1-alpine3.16"
              :exposed-ports [5432]
              :env-vars      {"POSTGRES_DB" dbname
                              "POSTGRES_USER" user
                              "POSTGRES_PASSWORD" password}}))

(defn start-container [c]
  (tc/start! c))

(defn stop-container [c]
  (tc/stop! c))

(defn copy-dir
  "Copy a directory from `from` to `to`. If `to` already exists, copy the directory
   to a directory with the same name as `from` within the `to` directory."
  [from to]
  (let [from-d-path (.getPath from)
        from-d-files (.list from)
        to-d-path (.getPath to)]
    (doseq [file from-d-files]
      (io/copy (io/file from-d-path file)
               (io/file to-d-path file)))))

(defn clean-directory
  ([directory-path] (clean-directory identity))
  ([directory-path file-pred]
   (let [files-to-delete (filter file-pred (file-seq directory-path))]
     (doseq [file files-to-delete] (io/delete-file (.getPath file))))))

