(ns ouch-it-hurts.patient-info-handlers-test
  (:require [ouch-it-hurts.config-reader.core :as cr]
            [ouch-it-hurts.relocatus.core :as relocat]
            [ouch-it-hurts.web.handlers.patient-info :as handlers]
            [ouch-it-hurts.db.core :as db]
            [ouch-it-hurts.helpers.core :as th]
            [ouch-it-hurts.helpers.gens :as tg]
            [ouch-it-hurts.specs :as sp]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]))

(def ^:dynamic *ds*)

(defn- clear-patients-info-table [ds]
  (jdbc/execute! ds ["delete from patients.info"]))

(defn- get-migration-dir [config]
  (-> (cr/read-config config [:relocatus/migrations])
      (get-in [:relocatus/migrations :migration-dir])
      (io/resource)
      (io/as-file)))

(defn- apply-migrations [cfg]
  (relocat/init-migration-table cfg)
  (relocat/migrate cfg))

(defn- once [work]
  (let [config (-> (cr/read-config "config.edn" [:db/connection :relocatus/migrations])
                   (cr/resolve-props))
        db-config (:db/connection config)
        relocat-config (:relocatus/migrations config)
        container (-> db-config
                      (select-keys [:dbname :user :password])
                      (th/create-container)
                      (th/start-container))
        {:keys [host mapped-ports]} container
        container-db-cfg (-> db-config
                             (assoc :host host)
                             (assoc :port (get mapped-ports 5432)))
        container-relocat-cfg (-> relocat-config
                                  (assoc-in [:db :host] host)
                                  (assoc-in [:db :port] (get mapped-ports 5432)))
        datasource (jdbc/get-datasource container-db-cfg)
        test-migrations-dir (get-migration-dir "config.edn")
        system-migrations-dir (get-migration-dir "system.edn")]
    (th/copy-dir system-migrations-dir test-migrations-dir)
    (apply-migrations container-relocat-cfg)
    (db/init-db-conn container-db-cfg)
    (work)
    (th/clean-directory test-migrations-dir #(s/ends-with? % ".sql"))
    (th/stop-container container)))

(defn- each [work]
  (work)
  (clear-patients-info-table @db/ds))

(use-fixtures :once once)
(use-fixtures :each each)

(defn- bd->str [{:keys [birth-date] :as p}]
  (if-not birth-date p
          (assoc p :birth-date (.toString birth-date))))

(defn- request-body
  ([b] (request-body {} b))
  ([r b] (assoc-in r [:app/request :body] b)))
(defn- request-query
  ([q] (request-query {} q))
  ([r q] (assoc-in r [:app/request :query-params] q)))
(defn- request-path
  ([p] (request-path {} p))
  ([r p] (assoc-in r [:app/request :path-params] p)))

(deftest add-patient-info-test
  (testing "successful"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))
          {:keys [status body]} (handlers/add-new (request-body (bd->str (tg/patient-gen))))]
      (is (= status 200))
      (is (contains? body :id))
      (is (spec/valid? :ouch-it-hurts.specs/add-patient-response body))))

  (testing "fail"
    (is (not= (:status (handlers/add-new (request-body {}))) 200))))

(deftest fetch-by-filter-test
  (testing "paging requires"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))]
      (handlers/add-new (request-body (bd->str (tg/patient-gen))))
      (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))]
        (is (= status 200))
        (is (= (count (:data body)) 1))
        (is (= (:total body) 1))
        (is (spec/valid? :ouch-it-hurts.specs/query-response body)))))

  (testing "fail"
    (is (not= (:status (handlers/get-all (request-query {:filters {}}))) 200))
    (is (not= (:status (handlers/get-all (request-query {:sorting {}}))) 200))
    (is (not= (:status (handlers/get-all (request-query {:sorting {:id :asc} :filters {}}))) 200))))

(deftest get-by-id-test
  (testing "successful"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))
          {:keys [status body] :as added} (handlers/add-new (request-body (bd->str (tg/patient-gen))))
          by-id (handlers/get-by-id (request-path {:id (:id body)}))]
      (is (= (:status by-id) 200))
      (is (spec/valid? :ouch-it-hurts.specs/get-patient-by-id-response (:body by-id)))))

  (testing "fail"
    (is (not= (:status (handlers/get-by-id (request-path {}))) 200))))

(deftest update-test
  (testing "successful"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))
          {:keys [body]} (handlers/add-new (request-body (bd->str (tg/patient-gen))))
          {:keys [status body]} (handlers/update-info (-> (request-path (:id body))
                                                          (request-body (assoc body :address (tg/address-gen)))))]
      (is (= status 200))
      (is (spec/valid? :ouch-it-hurts.specs/edit-patient-response body))))

  (testing "fail"
    (is (not= (:status (handlers/update-info (-> (request-path {})
                                                 (request-body {})))) 200))))

(deftest delete-test
  (testing "successful"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))
          added (handlers/add-new (request-body (bd->str (tg/patient-gen))))
          {:keys [status body] :as deleted} (handlers/delete (request-path {:id (get-in added [:body :id])}))]
      (is (= status 200))
      (is (spec/valid? :ouch-it-hurts.specs/delete-patient-response body))))

  (testing "fail"
    (is (not= (:status (handlers/delete (request-path {}))) 200))))

(deftest restore-test
  (testing "successful"
    (let [{:keys [status body]} (handlers/get-all (request-query {:paging {:page-number 1 :page-size 10}}))
          _ (is (= status 200))
          _ (is (empty? (:data body)))
          _ (is (zero? (:total body)))
          {:keys [body]} (handlers/add-new (request-body (bd->str (tg/patient-gen))))
          _ (handlers/delete (request-path {:id (:id body)}))
          {:keys [status body]} (handlers/restore-by-id (request-path {:id (:id body)}))]
      (is (= status 200))
      (is (spec/valid? :ouch-it-hurts.specs/restore-patient-response body))))

  (testing "fail"
    (is (not= (:status (handlers/restore-by-id (request-path {}))) 200))))


