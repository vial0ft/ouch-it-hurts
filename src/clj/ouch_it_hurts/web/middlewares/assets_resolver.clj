(ns ouch-it-hurts.web.middlewares.assets-resolver
  (:require [ouch-it-hurts.web.http-responses.core :as http-resp]
            [clojure.java.io :as io]
            [pantomime.mime :as mime]
            [pantomime.extract :as p-extract]))

(defn- xf [uri]
  (comp
   (map #(str % uri))
   (map #(io/resource %))
  ))

(defn- is-file? [url]
  (.isFile (io/as-file url)))

(defn- some-asset [uri asset-dirs]
  (->> (into []  (xf uri) asset-dirs)
       (some #(if (and (not (nil? %)) (is-file? %)) %))))


(defn assets-resolver-wrapper [handler asset-resource-dirs]
  (fn [req]
    (if-some [asset (some-asset (:uri req) asset-resource-dirs)]
      (let [mime-type (mime/mime-type-of asset)]
        (-> (http-resp/ok (slurp asset)) (http-resp/with-headers {"content-type" mime-type})))
      (handler req))))
