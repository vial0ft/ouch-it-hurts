(ns ouch-it-hurts.web.middlewares.assets-resolver
  (:require [ouch-it-hurts.web.http-responses.core :as http-resp]
            [clojure.java.io :as io]))



(defn- xf [uri]
  (comp
   (map #(str % uri))
   (map #(io/resource %))
  ))

(defn- is-file? [url]
  (.isFile (io/as-file url)))

(defn- some-asset [uri asset-dirs]
  (->> (into []
             (xf uri)
             asset-dirs)
       (some #(if (and (not (nil? %)) (is-file? %)) %))))




(defn assets-resolver-wrapper [handler asset-resource-dirs]
  (fn [req]
    (if-some [asset (some-asset (:uri req) asset-resource-dirs)]
      (http-resp/ok (slurp asset))
      (handler req))))
