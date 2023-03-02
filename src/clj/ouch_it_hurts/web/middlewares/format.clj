(ns ouch-it-hurts.web.middlewares.format
  (:require
   [cheshire.core :as json]
   [clojure.string :as s]))


(def ^:private key-keyword-f (map (fn [[k v]] {(keyword k) v})))

(def ^:private xf
  (comp
   (map #(s/split % #"="))
   key-keyword-f
   ))

(defn- reducer-f [acc [pair-k pair-v]]
  (if (get acc pair-k)
    (update acc pair-k #(vec (flatten [%1 %2])) pair-v)
    (assoc acc pair-k pair-v)))


(defn- group-query-params [query-str]
  (let [pairs  (-> (java.net.URLDecoder/decode query-str)
                   (s/split #"&"))
        seq-pairs (transduce xf into [] pairs)]
    (reduce reducer-f {} seq-pairs)))

;;
;; Request formatters
;;;;


(defn format-query-string [handler]
  (fn [req]
    (handler (if-let [query-str (:query-string req)]
               (assoc-in req [:app/request :query-params] (group-query-params query-str))
               req))))

(defn format-request-body [handler]
  (fn [{:keys [body headers] :as req}]
    (handler (if-not (nil? body)
               (case (get headers "content-type")
                 "application/json" (let [req-body (transduce
                                                    key-keyword-f
                                                    into {}
                                                    (-> (slurp body)
                                                        (json/decode)))]
                                      (assoc-in req [:app/request :body] req-body))
                 req)
               req))
    ))

(defn format-response-body [handler]
  (fn [req]
    (let [response (handler req)
          formated-resp (case (get-in response [:headers "content-type"])
                 "application/json" (update response :body json/encode)
                 response
                 )]
      formated-resp)))

