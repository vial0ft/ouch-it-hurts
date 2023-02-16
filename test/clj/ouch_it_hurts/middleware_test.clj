(ns ouch-it-hurts.middleware-test
  (:require [ouch-it-hurts.web.middlewares.format :refer :all]
            [cheshire.core :as json]
            [clojure.test :refer :all]))

(defn- handler-f [params-key]
  (fn [req]
  (get-in req [:app/request params-key])))


(defn- query-string-handler []
  (handler-f :query-params))

(defn- request-body-handler []
  (handler-f :body))


(defn- byte-input-stream [obj]
  (let [json (json/encode obj)
        b-json (.getBytes json)]
  (org.httpkit.BytesInputStream. b-json (count b-json)))
  )

(deftest format-query-string-middlewares-tests
  (testing
      "According `query-string-handler` should return map with `key1` and `key2`"
    (let [request-like {:query-string "key1=qwe&key2=foo"}
          handler-f (format-query-string (query-string-handler))
          {:keys [key1 key2] :as response-like} (handler-f request-like)]
      (is (= key1 "qwe"))
      (is (= key2 "foo"))
      ))

  (testing
      "According `query-string-handler` should return map with `key1` as key and seq with several values"
    (let [request-like {:query-string "key1=qwe&key1=foo"}
          handler-f (format-query-string (query-string-handler))
          {:keys [key1] :as response-like}  (handler-f request-like)
          params-set (into #{} key1)]
      (is (count key1) 2)
      (is (contains? params-set "qwe"))
      (is (contains? params-set "foo"))
      ))

  (testing
      "No `:query-params` in the response when `:query-string` is `nil`"
    (let [request-like {:query-string nil}
          handler-f (format-query-string (query-string-handler))]
      (is (nil? (handler-f request-like))))
    )
  )


(deftest format-request-body-middleware-test
  (testing
      "According `request-body-handler` should return map with `key1` and `key2`"
    (let [request-like {:headers {"content-type" "application/json"}
                        :body (byte-input-stream {:key1 1 :key2 "2"})}
          handler-f (format-request-body (request-body-handler))
          {:keys [key1 key2]} (handler-f request-like)]
      (is (= key1 1))
      (is (= key2 "2"))
      )
    )

  (testing
      "No `:body` in the response when `:body` is `nil`"
    (let [request-like {:headers {"content-type" "application/json"}}
          handler-f (format-request-body (request-body-handler))]
      (is (nil? (handler-f request-like)))
      )
    )
  )


(defn- handler-echo-json-response
  [req]
  {:status  200
   :headers {"Content-type" "application/json"}
   :body req})


(defn- handler-echo-response
  [req]
  {:status 200
   :body req})

(deftest format-response-body-middleware-test
  (testing
      "According `handler-echo-response` should return map  with key `:body` with json-string
       when response contains `:headers {\"Content-type\" \"application/json\"}`"
    (let [request-like {:params [1 2 3 4]}
          handler-f (format-response-body handler-echo-json-response)
          {:keys [body]} (handler-f request-like)]
    (is (= body (json/encode request-like)))))

  (testing
      "According `handler-echo-response` should return map  with `:body` as is
       when response not contains `:headers {\"Content-type\" \"application/json\"}`"
    (let [request-like {:params [1 2 3 4]}
          handler-f (format-response-body handler-echo-response)
          {:keys [body]} (handler-f request-like)]
      (is (= body request-like))))
  )
