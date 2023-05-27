(ns ouch-it-hurts.patients-info-test
  (:require [ouch-it-hurts.config-reader.core :as cr]
            [ouch-it-hurts.relocatus.core :as relocat]
            [ouch-it-hurts.patients-info.service :as service]
            [ouch-it-hurts.db.core :as db]
            [ouch-it-hurts.helpers.core :as th]
            [ouch-it-hurts.specs :as sp]
            [clojure.spec.alpha :as spec]
            [clojure.test.check.generators :as gen]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.test :refer :all]
            [next.jdbc :as jdbc]))

(def ^:dynamic *ds*)


(defn- oms-gen []
  (s/join (gen/generate (gen/vector gen/pos-int sp/oms-numbers-count) 9)))

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


(defn- clean-directory [directory-path]
  (let [files-to-delete (filter #(s/ends-with? % ".sql") (file-seq directory-path))]
    (doseq [file files-to-delete]
      (io/delete-file (.getPath file)))))

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
    (clean-directory test-migrations-dir)
    (th/stop-container container)))

(defn- each [work]
  (work)
  (clear-patients-info-table @db/ds))

(use-fixtures :once once)
(use-fixtures :each each)



(deftest successful-add-patient
  (testing "Successful add patient"
    (let [patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:first-name patient-firstname})
          _ (is id)
          get-by-id (service/get-by-id id)]
      (is (some? get-by-id))
      (is (= id (:id get-by-id)))
      )))

(deftest fail-add-patient-if-patient-exists-with-same-oms
  (testing "fail add patient"
    (let [patient-oms (oms-gen)
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          add-result (service/add-patient-info {:oms patient-oms})
          _ (is (contains? add-result :id))
          get-by-id (service/get-by-id (:id add-result))
          _ (is (some? get-by-id))]
      (is (thrown? Exception (service/add-patient-info {:oms patient-oms})))
      )))


(deftest successful-update-patient-info
  (testing "successful update"
    (let [patient-oms (oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
           get-by-id (service/get-by-id id)
          _ (is (some? get-by-id))
          _ (service/update-patient-info (:id get-by-id) (assoc get-by-id :first-name patient-firstname))
          {:keys [first-name oms] :as get-after-update} (service/get-by-id (:id get-by-id))]
      (is (= (:id get-after-update) id))
      (is (= first-name patient-firstname))
      (is (= oms patient-oms))
      )))


(deftest update-notexisted-patient-info
  (testing "fail update"
    (let [patient-oms (oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          not-existed-id 42
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          get-by-id (service/get-by-id not-existed-id)]
      (is (nil? get-by-id))
      (is (thrown? Exception (service/update-patient-info not-existed-id {:first-name patient-firstname}))))
      ))

(deftest update-patient-info-by-existing-oms-should-fail
  (testing "fail update"
    (let [patient-oms1 (oms-gen)
          patient-firstname1 (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          patient-oms2 (oms-gen)
          patient-firstname2 (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          p1 (service/add-patient-info {:first-name patient-firstname1 :oms patient-oms1})
          p2 (service/add-patient-info {:first-name patient-firstname2 :oms patient-oms2})
          have-to-be-2-patients (service/get-all {:filters {}})
          _ (is (and (= (count (:data have-to-be-2-patients)) 2)
                     (= (:total have-to-be-2-patients) 2)))]
      (is (thrown? Exception (service/update-patient-info (:id p1) (assoc p1 :oms (:oms p2))))))
    ))

(deftest successful-delete-patient-info
  (testing "Successful delete"
    (let [patient-oms (oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          get-by-id (service/get-by-id id)
          _ (is (some? get-by-id))
          _ (service/delete-patient-info id)]
      (is (:deleted (service/get-by-id id))))
    ))

(deftest delete-notexisted-patient-info
  (testing "fail delete"
    (let [not-existed-id 42
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          get-by-id (service/get-by-id not-existed-id)]
      (is (nil? get-by-id))
      (is (thrown? Exception (service/delete-patient-info not-existed-id))))
    ))

