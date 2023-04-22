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

(defn- make-key-path [k]
  (let [root-key  (->> (re-find #"([A-Za-z0-9-]+)\[" (name k)) (second))
        path-keys (->> (re-seq #"\[([A-Za-z0-9-]+)\]" (name k)) (map second))]
    (vec (map keyword (cons root-key path-keys)))))

(defn- reducer-f [acc [pair-k pair-v]]
  (let [key-path (if-not (empty? (re-seq #"\[([A-Za-z0-9-]+)\]" (name pair-k)))
                    (make-key-path pair-k)
                    [pair-k])
        nilable-value (if (= pair-v "null") nil pair-v)
        existed-value (get-in acc key-path)]
      (if existed-value
        (update-in acc key-path #(vec (flatten [%1 %2])) nilable-value)
        (assoc-in acc key-path nilable-value))))


(defn- group-query-params [query-str]
  (let [pairs  (-> (java.net.URLDecoder/decode query-str) (s/split #"&"))
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

