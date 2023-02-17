(ns ouch-it-hurts.config-reader-test
  (:require
   [ouch-it-hurts.config-reader.core :as cr]
   [clojure.test :refer :all]))

(deftest simple-config-reader-test

  (testing
      "Read existing config from resources with a special name"
    (let [config  (cr/read-config "config.edn")]
      (is (not-empty config))
      ))

  (testing
      "Read existing config with special keys only"
    (let [config (cr/read-config "config.edn" [:server/http])]
      (is (contains? config :server/http))
      (is (not (contains? config :routes/api)))
      )
    )

  (testing
      "Throw exception for not existed config"
    (is (thrown? IllegalArgumentException (cr/read-config "not-existed-config.edn")))
    )
  )

(deftest resolving-config-properties
  (testing
      "Resolve props with environment variable e.g. PATH"
    (let [resolved (cr/resolve-props {:env "PATH" :default ""})]
      (is (not (empty? resolved))))
    )

  (testing
      "Resolve props with not existed environment variable should return `:default`"
    (let [resolved (cr/resolve-props {:env "NOT_EXISTED_ENV_VARIABLE" :default "DEFAULT_VARIABLE"})]
      (is (not (empty? resolved)))
      (is (= resolved "DEFAULT_VARIABLE")))
    )

  (testing
      "Resolve props for simple value"
    (let [resolved (cr/resolve-props 42)]
      (is (= resolved 42)))
    )

  (testing
      "Resolve props for simple map value"
    (let [resolved (cr/resolve-props {:meaning-of-life 42})]
      (is (= (:meaning-of-life resolved) 42)))
    )

  (testing
      "Resolve props for nested config map"
    (let [resolved (cr/resolve-props {:first-level
                                      {:second-level-path
                                       {:env "PATH" :default ""}
                                       :second-level-value 42}})]
      (is (not (empty? (get-in resolved [:first-level :second-level-path]))))
      (is (= (get-in resolved [:first-level :second-level-value]) 42)))
    )

  (testing
      "Resolve props with `nil` if environment variable not exists and `:default` absents"
    (let [resolved (cr/resolve-props {:env "NOT_EXISTED_ENV_VARIABLE"})]
      (is (nil? resolved)))
    )
  )
