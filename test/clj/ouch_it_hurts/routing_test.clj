(ns ouch-it-hurts.routing-test
  (:require
   [ouch-it-hurts.routing :as r]
   [clojure.test :refer :all]))



(deftest single-route-match-test
  (testing
      "Uris are matched"
    (let [requet-uri "/foo/bar/baz"
          route-uri "/foo/bar/baz"] 
      (is (:match? (r/match-uri requet-uri route-uri)))
      ))

  (testing
      "Uris aren't matched"
    (let [requet-uri "/foo/bar/bazzzzz"
          route-uri "/foo/bar/baz"] 
      (is (not (:match? (r/match-uri requet-uri route-uri))))
      ))

  (testing
      "Uris are matched with `*` in `route-uri`"
    (let [requet-uri1 "/foo/bar/bazzzzz"
          requet-uri2 "/foo/bar/baz/qwe"
          route-uri "/foo/bar/*"] 
      (is (:match? (r/match-uri requet-uri1 route-uri)))
      (is (:match? (r/match-uri requet-uri2 route-uri)))
      )
    )

  (testing
      "Uris are matched with `:id` path-param"
    (let [requet-uri "/foo/bar/baz"
          route-uri "/foo/:id/baz"] 
      (is (:match? (r/match-uri requet-uri route-uri)))
      (is (= (get-in (r/match-uri requet-uri route-uri) [:params :id]) "bar"))
      )
    )

  (testing
      "Uris are matched with `:id` and `:name` path-params"
    (let [requet-uri "/foo/bar/baz"
          route-uri "/foo/:id/:name"] 
      (is (:match? (r/match-uri requet-uri route-uri)))
      (is (= (get-in (r/match-uri requet-uri route-uri) [:params :id]) "bar"))
      (is (= (get-in (r/match-uri requet-uri route-uri) [:params :name]) "baz"))
      )
    )
  )

(deftest request-route-match-test
  (testing
      "Request should match and handler should return `:foo-bar-baz-get`"
     (let [request  {:uri "/foo/bar/baz" :request-method :get}
           routes [["/"
                    {:get {:handler (fn [req] :root-get)}
                     :post {:handler (fn [req] :root-post)}}]
                   ["/foo/bar"
                    {:get {:handler (fn [req] :foo-bar-get)}
                     :post {:handler (fn [req] :foo-bar-post)}}]
                   ["/foo/bar/baz"
                    {:get {:handler (fn [req] :foo-bar-baz-get)}
                     :post {:handler (fn [req] :foo-bar-baz-post)}}]
                   ["/foo/bar/bazzzz"
                    {:get {:handler (fn [req] :foo-bar-bazzzz-get)}
                     :post {:handler (fn [req] :foo-bar-bazzzz-post)}}]]
           [path-params handler-f :as result] (r/get-path-params-and-handler request routes)]
       (is (not (empty? result)))
       (is (map? path-params))
       (is (= (handler-f :request) :foo-bar-baz-get)
       )))

  (testing
      "Request should match with params and handler should return `42`"
     (let [request  {:uri "/foo/bar/42" :request-method :get}
           routes [["/"
                    {:get {:handler (fn [req] :root-get)}
                     :post {:handler (fn [req] :root-post)}}]
                   ["/foo/bar"
                    {:get {:handler (fn [req] :foo-bar-get)}
                     :post {:handler (fn [req] :foo-bar-post)}}]
                   ["/foo/bar/baz"
                    {:get {:handler (fn [req] :foo-bar-baz-get)}
                     :post {:handler (fn [req] :foo-bar-baz-post)}}]
                   ["/foo/bar/:number"
                    {:get {:handler (fn [req] (:number req))}
                     :post {:handler (fn [req] :foo-bar-number-post)}}]]
           [path-params handler-f :as result] (r/get-path-params-and-handler request routes)]
       (is (not (empty? result)))
       (is (contains? path-params :number))
       (is (= (handler-f path-params) "42")
       )))
  )




