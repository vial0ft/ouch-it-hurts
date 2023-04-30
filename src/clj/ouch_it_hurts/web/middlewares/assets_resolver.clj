(ns ouch-it-hurts.web.middlewares.assets-resolver
  (:require [ouch-it-hurts.web.http-responses.core :as http-resp]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [pantomime.mime :as mime]
            [clojure.string :as s]))

(defn- xf [uri]
  (comp
   (map #(str % uri))
   (map #(io/resource %))
   (filter some?)
  ))



(defn- some-asset [uri asset-dirs]
  (when-not (= uri "/")
    (let [resources (into [] (xf uri) asset-dirs)]
      (log/infof "res %s" resources)
      (when-first [r resources]
        (log/infof "r %s" r)
        {:resource r :mime-type (mime/mime-type-of r)}))))

(defn assets-resolver-wrapper [handler asset-resource-dirs]
  (fn [req]
    (if-some [result (some-asset (:uri req) asset-resource-dirs)]
      (-> (:resource result)
          (io/input-stream)
          (http-resp/ok)
          (http-resp/with-headers {"content-type" (:mime-type result)}))
    (handler req))))

