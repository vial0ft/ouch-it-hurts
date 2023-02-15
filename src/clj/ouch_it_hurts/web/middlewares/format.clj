(ns ouch-it-hurts.web.middlewares.format
  (:require
   [cheshire.core :as json]
   [clojure.string :as s]))


(def ^:private xf
  (comp
   (map #(s/split % #"="))
   (map (fn [[k v]] {(keyword k) v}))
   ))

(defn reducer-f [acc [pair-k pair-v]]
  (if (get acc pair-k)
    (update acc pair-k #(vec (flatten [%1 %2])) pair-v)
    (assoc acc pair-k pair-v)))


(defn- group-query-params [query-str]
  (let [pairs  (-> (java.net.URLDecoder/decode query-str)
                   (s/split #"&"))
        seq-pairs (transduce xf into [] pairs)]
    (reduce reducer-f {} seq-pairs)))


(defn format-query-string [handler]
  (fn [req]
    (handler (if-let [query-str (:query-string req)]
               (assoc-in req [:app/request :query-params] (group-query-params query-str))
               req))))

(defn format-request-body [handler]
  (fn [req]
    (handler (if-let [body (:body req)]
               (case (get-in req [:headers "content-type"])
                 "application/json" (assoc-in req [:app/request :body] (json/decode
                                                                        (slurp body)))
                 req)
               req))
    ))

(defn format-response-body [handler]
  (fn [req]
    (let [response (handler req)]
      (case (get-in response [:headers "Content-type"])
        "application/json" (update response :body json/encode)
        response
        ))))


(comment


  (def xf
    (comp
     (map #(s/split % #"="))
     (map (fn [[k v]] {(keyword k) v}))
     ))

  (defn reducer-f [acc [pair-k pair-v]]
    (if (get acc pair-k)
      (update acc pair-k #(vec (flatten [%1 %2])) pair-v)
      (assoc acc pair-k pair-v)))

  (let [pairs  (-> (java.net.URLDecoder/decode "foo=1&bar=%22asd%22&asd=1&asd=2&wwwa=%22asd%22")
                   (s/split #"&"))
        seq-pairs (transduce xf into [] pairs)]
    (reduce reducer-f {} seq-pairs))


  (conj 1 2)

  (-> (java.net.URLDecoder/decode "foo=1&bar=%22asd%22&asd=1&asd=2&wwwa=asd")
      (s/split #"&")
      (group-by (fn[pair] (first (s/split pair #"="))) pair))

  )
