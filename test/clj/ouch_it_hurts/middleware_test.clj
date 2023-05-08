(ns ouch-it-hurts.middleware-test
  (:require [ouch-it-hurts.web.middlewares.format :refer :all]
            [ouch-it-hurts.web.middlewares.exceptions :refer :all]
            [ouch-it-hurts.web.middlewares.core :refer [wrap-handler]]
            [ouch-it-hurts.web.middlewares.assets-resolver :refer :all]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [ouch-it-hurts.web.http-responses.core :as http-resp]
            [clojure.test :refer :all]))

(defn- handler-f
  ([] (fn [req] (get req :app/request)))
  ([params-key] (fn [req] (get-in req [:app/request params-key]))))

(defn- query-string-handler []
  (handler-f :query-params))

(defn- request-body-handler []
  (handler-f :body))

(defn- byte-input-stream [obj]
  (let [json (json/encode obj)
        b-json (.getBytes json)]
    (org.httpkit.BytesInputStream. b-json (count b-json))))

(deftest format-query-string-middlewares-tests
  (testing
   "According `query-string-handler` should return map with `key1` and `key2`"
    (let [request-like {:query-string "key1=qwe&key2=foo"}
          handler-f (format-query-string (query-string-handler))
          {:keys [key1 key2] :as response-like} (handler-f request-like)]
      (is (= key1 "qwe"))
      (is (= key2 "foo"))))

  (testing
   "According `query-string-handler` should return map with `key1` as key and seq with several values"
    (let [request-like {:query-string "key1=qwe&key1=foo"}
          handler-f (format-query-string (query-string-handler))
          {:keys [key1] :as response-like}  (handler-f request-like)
          params-set (into #{} key1)]
      (is (= (count key1) 2))
      (is (contains? params-set "qwe"))
      (is (contains? params-set "foo"))))

  (testing
   "According `query-string-handler` should return map where key is `key1` and value is map when query does have object"
    (let [request-like {:query-string "key1[id]=1&key1[date]=2023-01-01&key1[flag]=true"}
          handler-f (format-query-string (query-string-handler))
          result (handler-f request-like)]
      (is (= (count result) 1))
      (is (and (some? (get-in result [:key1])) (map? (get-in result [:key1]))))
      (is (and (some? (get-in result [:key1 :id])) (= (get-in result [:key1 :id]) "1")))
      (is (and (some? (get-in result [:key1 :date])) (= (get-in result [:key1 :date]) "2023-01-01")))
      (is (and (some? (get-in result [:key1 :flag])) (= (get-in result [:key1 :flag]) "true")))))

  (testing
   "If for SOME REASON someone pass an object through query params. According `query-string-handler` should return deep map"
    (let [request-like {:query-string "key1[a][b][c]=1"}
          handler-f (format-query-string (query-string-handler))
          result (handler-f request-like)]
      (is (and (some? (get-in result [:key1 :a :b :c])) (= (get-in result [:key1 :a :b :c]) "1")))))

  (testing
   "No `:query-params` in the response when `:query-string` is `nil`"
    (let [request-like {:query-string nil}
          handler-f (format-query-string (query-string-handler))]
      (is (nil? (handler-f request-like))))))

(deftest format-request-body-middleware-test
  (testing
   "According `request-body-handler` should return map with `key1` and `key2`"
    (let [request-like {:headers {"content-type" "application/json"}
                        :body (byte-input-stream {:key1 1 :key2 "2"})}
          handler-f (format-request-body (request-body-handler))
          {:keys [key1 key2]} (handler-f request-like)]
      (is (= key1 1))
      (is (= key2 "2"))))

  (testing
   "No `:body` in the response when `:body` is `nil`"
    (let [request-like {:headers {"content-type" "application/json"}}
          handler-f (format-request-body (request-body-handler))]
      (is (nil? (handler-f request-like))))))

(defn- echo-ok-json
  [req]
  (http-resp/json-ok req))

(deftest format-response-body-middleware-test
  (testing
   "According `handler-echo-response` should return map  with key `:body` with json-string
       when response contains `:headers {\"Content-type\" \"application/json\"}`"
    (let [request-like {:params [1 2 3 4]}
          handler-f (format-response-body echo-ok-json)
          {:keys [body]} (handler-f request-like)]
      (is (= body (json/encode request-like)))))

  (testing
   "According `handler-echo-response` should return map  with `:body` as is
       when response not contains `:headers {\"Content-type\" \"application/json\"}`"
    (let [request-like {:params [1 2 3 4]}
          handler-f (format-response-body http-resp/ok)
          {:keys [body]} (handler-f request-like)]
      (is (= body request-like)))))

(defn throwing-handler
  [_]
  (throw (ex-info "Ooops!" {:reason "Something went wrong"})))

(deftest exception-handle-wrapper-test
  (testing "Wrapper should catch Exception and return `internal-error` status code"
    (let [request-like {:headers {"header1" "value"}
                        :uri "/"
                        :request-method :get}
          handler-f (exceptions-handler-wrapper throwing-handler)
          {:keys [status body]} (handler-f request-like)]
      (is (= status (:status (http-resp/internal-server-error))))
      (is (= (:error body) {:message "Ooops!"
                            :details {:reason "Something went wrong"}})))))

(deftest appliation-middlewares-test
  (testing "Composition of application's middlewares should handle exceptions"
    (let [request-like {:headers {"accept" "*/*" "content-type" "application/json"},
                        :character-encoding "utf8",
                        :uri "/patients",
                        :query-string "foo=1&asd=1&wwwa=asd",
                        :body (byte-input-stream {:foo "bar"}) ,
                        :scheme :http,
                        :request-method :get}
          handler-f (wrap-handler throwing-handler)
          {:keys [status body]} (handler-f request-like)]
      (is (= status (:status (http-resp/internal-server-error))))
      (is (= (:error body)
             {:message "Ooops!"
              :details {:reason "Something went wrong"}}))))

  (testing "Composition of application's middlewares should return `:app/request` info"
    (let [request-like {:headers {"accept" "*/*" "content-type" "application/json"},
                        :character-encoding "utf8",
                        :uri "/patients",
                        :query-string "foo=1&asd=1&wwwa=asd",
                        :body (byte-input-stream {:foo "bar"}) ,
                        :scheme :http,
                        :request-method :get}
          handler (wrap-handler (handler-f))
          result (handler request-like)]
      (is (= (:body result) {:foo "bar"}))
      (is (= (:query-params result) {:foo "1"
                                     :asd "1"
                                     :wwwa "asd"})))))

(deftest asset-resolver-middleware-test
  (testing "Resolver should ignore `handler` and return asset by `:uri`"
    (let [asset-dir "public"
          resource-path "/test_resource.txt"
          file-content (slurp (io/resource (str asset-dir resource-path)))
          request-like {:uri resource-path}
          handler (assets-resolver-wrapper nil [asset-dir])
          {:keys [status body]} (handler request-like)]
      (is (= status (:status (http-resp/ok))))
      (with-open [r (clojure.java.io/reader body)]
        (is (= (slurp r) file-content)))))

  (testing "Resolver should use `handler` if asset not found  by `:uri`"
    (let [asset-dir "public"
          resource-path "/not_existed_file.txt"
          request-handler (fn [req] (http-resp/ok :response-but-not-asset))
          request-like {:uri resource-path}
          handler (assets-resolver-wrapper request-handler [asset-dir])
          {:keys [status body]} (handler request-like)]
      (is (= status (:status (http-resp/ok))))
      (is (= body :response-but-not-asset)))))
