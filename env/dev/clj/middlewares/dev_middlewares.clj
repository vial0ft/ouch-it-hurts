(ns middlewares.dev-middlewares
  (:require
   [ouch-it-hurts.web.middlewares.core :as middlewares]
   [ouch-it-hurts.web.middlewares.cors :as cors]))

(defn wrap-handler-dev [handler]
  (-> handler
      (middlewares/wrap-handler)
      (cors/cors)
      (middlewares/wrap-handler-with-logging)
      ))








