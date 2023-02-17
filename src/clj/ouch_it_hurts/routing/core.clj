(ns ouch-it-hurts.routing.core
  (:require [clojure.string :as s]))

(defn- step-match [curr-request-uri-part curr-route-uri-part]
  (cond
    (and (nil? curr-request-uri-part) (nil? curr-route-uri-part)) {:match? true :next? false}
    (= curr-route-uri-part "*") {:match? true :next? false}
    (or (nil? curr-request-uri-part) (nil? curr-route-uri-part)) {:match? false :next? false}
    (s/starts-with? curr-route-uri-part ":") {
                                              :match? true
                                              :next? true
                                              :step-params {
                                                            (-> curr-route-uri-part
                                                                (subs 1)
                                                                (keyword))
                                                            curr-request-uri-part
                                                            }}
    (= curr-route-uri-part curr-request-uri-part) {:match? true :next? true}
    :else {:match? false :next? false}
    ))

(defn match-uri [request-uri route-uri]
  (let [splitted-request-uri (s/split request-uri #"/")
        splitted-route-uri (s/split route-uri #"/")]
    (loop [sp-req-uri splitted-request-uri
           sp-rt-uri splitted-route-uri
           params {}]
      (let [curr-request-uri-part (first sp-req-uri)
            curr-route-uri-part (first sp-rt-uri)
            {:keys [match? next? step-params]} (step-match curr-request-uri-part curr-route-uri-part)]
        (if-not match? {:match? false}
                (if-not next? {:match? true :params params}
                        (recur (next sp-req-uri) (next sp-rt-uri) (merge params step-params)))))
      )))


(defn get-path-params-and-handler [request routes]
  (loop [req request
         rts routes]
    (if (empty? rts) []
        (let [[uri handlers-map] (first rts)
              match-result (match-uri (:uri req) uri)]
          (if (:match? match-result)
            (let [handler-f (get-in handlers-map [(:request-method req) :handler])]
              (if-not (nil? handler-f) [(:params match-result) handler-f] []))
            (recur req (next rts)))))))

