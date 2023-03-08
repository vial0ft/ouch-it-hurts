(ns ouch-it-hurts.web.routes.pages
  (:require [ouch-it-hurts.web.http-responses.core :as http-resp]
            [clojure.java.io :as io]))




(defn- home [_]
  (http-resp/page (slurp (io/resource "index.html")))
  )


(defn pages []
  [
   ["/" {:get {:handler home}}]
   ])
