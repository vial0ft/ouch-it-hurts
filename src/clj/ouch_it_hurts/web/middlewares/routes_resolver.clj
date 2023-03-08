(ns ouch-it-hurts.web.middlewares.routes-resolver
  (:require
   [ouch-it-hurts.routing.core :as r]
   [ouch-it-hurts.web.http-responses.core :as http-responses]))


(defn- resolve-routing [routes]
  (fn [req]
    (let [[result details] (r/get-path-params-and-handler req routes)]
      (case [result details]
        [:error :not-found] (http-responses/not-found
                             (format "Resource not found %s %s" (:request-method req) (:uri req)))
        [:error :method-not-allowed] (http-responses/method-not-allowed
                                      (format "Method not Allowed %s %s" (:request-method req) (:uri req)))
        (let [[path-params handler :as result] details]
          (if (nil? handler) (http-responses/method-not-allowed (format "Method not Allowed %s %s" (:request-method req) (:uri req)))
              (handler (if-not (empty? path-params)
                         (assoc-in req [:app/request :path-params] path-params)
                         req))))))))


(defn routing [& routes]
  (resolve-routing (reduce into [] routes)))
