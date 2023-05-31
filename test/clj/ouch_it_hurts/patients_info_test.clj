(ns ouch-it-hurts.patients-info-test
  (:require [ouch-it-hurts.config-reader.core :as cr]
            [ouch-it-hurts.relocatus.core :as relocat]
            [ouch-it-hurts.patients-info.service :as service]
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
      (is (= id (:id get-by-id))))))

(deftest fail-add-patient-if-patient-exists-with-same-oms
  (testing "fail add patient"
    (let [patient-oms (tg/oms-gen)
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          add-result (service/add-patient-info {:oms patient-oms})
          _ (is (contains? add-result :id))
          get-by-id (service/get-by-id (:id add-result))
          _ (is (some? get-by-id))]
      (is (thrown? Exception (service/add-patient-info {:oms patient-oms}))))))

(deftest successful-update-patient-info
  (testing "successful update"
    (let [patient-oms (tg/oms-gen)
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
      (is (= oms patient-oms)))))

(deftest update-notexisted-patient-info
  (testing "fail update"
    (let [patient-oms (tg/oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          not-existed-id 42
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          get-by-id (service/get-by-id not-existed-id)]
      (is (nil? get-by-id))
      (is (thrown? Exception (service/update-patient-info not-existed-id {:first-name patient-firstname}))))))

(deftest update-patient-info-by-existing-oms-should-fail
  (testing "fail update"
    (let [patient-oms1 (tg/oms-gen)
          patient-firstname1 (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          patient-oms2 (tg/oms-gen)
          patient-firstname2 (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          p1 (service/add-patient-info {:first-name patient-firstname1 :oms patient-oms1})
          p2 (service/add-patient-info {:first-name patient-firstname2 :oms patient-oms2})
          have-to-be-2-patients (service/get-all {:filters {}})
          _ (is (and (= (count (:data have-to-be-2-patients)) 2)
                     (= (:total have-to-be-2-patients) 2)))]
      (is (thrown? Exception (service/update-patient-info (:id p1) (assoc p1 :oms (:oms p2))))))))

(deftest update-deleted-patient-info
  (testing "Can't update deleted patient's info"
    (let [patient-oms (tg/oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          get-by-id (service/get-by-id id)
          _ (is (some? get-by-id))
          _ (service/delete-patient-info (:id get-by-id))]
      (is (thrown? Exception (service/update-patient-info
                              (:id get-by-id)
                              (assoc get-by-id :first-name patient-firstname)))))))

(deftest successful-delete-patient-info
  (testing "Successful delete"
    (let [patient-oms (tg/oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          get-by-id (service/get-by-id id)
          _ (is (some? get-by-id))
          _ (service/delete-patient-info id)]
      (is (:deleted (service/get-by-id id))))))

(deftest delete-notexisted-patient-info
  (testing "fail delete"
    (let [not-existed-id 42
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          get-by-id (service/get-by-id not-existed-id)]
      (is (nil? get-by-id))
      (is (thrown? Exception (service/delete-patient-info not-existed-id))))))

(deftest delete-already-deleted-patient-info
  (testing "fail delete"
    (let [patient-oms (tg/oms-gen)
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          deleted-patient-info (service/delete-patient-info id)]
      (is (:deleted deleted-patient-info))
      (is (thrown? Exception (service/delete-patient-info id))))))

(deftest successful-restore-deleted-patient-info
  (testing "Restore test"
    (let [patient-oms (tg/oms-gen)
          patient-firstname (gen/generate (spec/gen :ouch-it-hurts.specs/first-name))
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          get-by-id (service/get-by-id id)
          _ (is (some? get-by-id))
          _ (service/delete-patient-info id)]
      (is (:deleted (service/get-by-id id)))
      (service/restore-patient-info id)
      (is (not (:deleted (service/get-by-id id)))))))

(deftest restore-notexisted-patient-info
  (testing "Fail restore"
    (let [not-existed-id 100500
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))]
      (is (thrown? Exception (service/restore-patient-info not-existed-id))))))

(deftest restore-notdeleted-patient-info
  (testing "Fail restore"
    (let [patient-oms (tg/oms-gen)
          {:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          {:keys [id]} (service/add-patient-info {:oms patient-oms})
          _ (is id)
          get-by-id (service/get-by-id id)]
      (is (some? get-by-id))
      (is (not (:deleted get-by-id)))
      (is (thrown? Exception (service/restore-patient-info id))))))

(defn- gen-string [cnt prefix]
  (->> (gen/vector gen/string-alphanumeric cnt)
       (gen/generate)
       (map #(str prefix %))))

(deftest fetch-patients-info-first-name-by-filters
  (testing "First name filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          name-prefix "John"
          count-of-items 2
          names (gen-string count-of-items name-prefix)
          _ (doseq [n names] (service/add-patient-info {:first-name n}))
          get-all-filter (service/get-all {:filters {:first-name name-prefix}})]
      (is (= (->> get-all-filter (map :data) (count))
             count-of-items)))))

(deftest fetch-patients-info-middle-name-by-filters
  (testing "Middle name filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          name-prefix "Alex"
          count-of-items 2
          names (gen-string count-of-items name-prefix)
          _ (doseq [n names] (service/add-patient-info {:middle-name n}))
          get-all-filter (service/get-all {:filters {:middle-name name-prefix}})]
      (is (= (->> get-all-filter (map :data) (count))
             count-of-items)))))

(deftest fetch-patients-info-last-name-by-filters
  (testing "Last name filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          name-prefix "Smith"
          count-of-items 2
          names (gen-string count-of-items name-prefix)
          _ (doseq [n names] (service/add-patient-info {:last-name n}))
          get-all-filter (service/get-all {:filters {:last-name name-prefix}})]
      (is (= (->> get-all-filter (map :data) (count))
             count-of-items)))))

(defn- count-of [value coll]
  (->> coll
       (filter #(= % value))
       (count)))

(defn- same-count-of? [value coll1 coll2]
  (= (count-of value coll1)
     (count-of value coll2)))

(defn- gen-from-set [cnt set]
  (->> (gen/sample (spec/gen set) 10)
       (take cnt)))

(deftest fetch-patients-info-sex-by-filters
  (testing "Sex filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          count-of-items 5
          variants (gen-from-set count-of-items (into #{} (filter #(not= "unknown" %) sp/sex-enum)))
          records (cons {:first-name "patient with unknown sex"} (map #(assoc {} :sex %) variants))
          _ (doseq [r records] (service/add-patient-info r))
          get-all-filter (service/get-all {:filters {:sex-opts sp/sex-filter-opts}})]
      (is (= (-> (:data get-all-filter) (count)) (inc count-of-items)))
      (is (same-count-of? "female" (map :sex records) (map :sex (:data get-all-filter))))
      (is (same-count-of? "male" (map :sex records) (map :sex (:data get-all-filter))))
      (is (= (same-count-of? nil (map :sex records) (map :sex (:data get-all-filter))))))))

(defn- inst->localdate [i]
  (-> (.toInstant i)
      (.atZone (java.time.ZoneId/systemDefault))
      (.toLocalDate)))

(defn- gen-date [cnt from to]
  (->> (gen/sample (spec/gen (spec/inst-in from to)) 100)
       (drop (- 100 cnt))
       (map inst->localdate)))

(deftest fetch-patients-birthdate-by-filters
  (testing "Birthdate filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          count-of-items 5
          from #inst "2010"
          to #inst "2020"
          from-local-str (.toString (inst->localdate from))
          to-local-str (.toString (inst->localdate to))
          dates (gen-date count-of-items from to)
          _ (doseq [d dates] (service/add-patient-info {:birth-date d}))]
      (is (= (-> (:data (service/get-all {:filters {:birth-date {:from from-local-str :to to-local-str}}})) (count))
             (count dates)))
      (is (= (-> (:data (service/get-all {:filters {:birth-date {:from from-local-str}}})) (count))
             (count dates)))
      (is (= (-> (:data (service/get-all {:filters {:birth-date {:to to-local-str}}})) (count))
             (count dates))))))

(deftest fetch-patients-info-address-by-filters
  (testing "Address filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          prefix "Utopia"
          count-of-items 5
          address-like (gen-string count-of-items prefix)
          _ (doseq [a address-like] (service/add-patient-info {:address a}))]
      (is (= (->> (:data (service/get-all {:filters {:address prefix}})) (count))
             count-of-items)))))

(deftest fetch-patients-info-oms-by-filters
  (testing "OMS filter test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          prefix "00000"
          count-of-items 5
          oms-seq   (->> (range count-of-items)
                         (map (fn [_] (tg/oms-gen)))
                         (map #(s/replace % #"^.{5}" prefix)))
          _ (doseq [o oms-seq] (service/add-patient-info {:oms o}))]
      (is (= (->> (:data (service/get-all {:filters {:oms prefix}})) (count))
             count-of-items)))))

(deftest fetch-patient-info-with-page-size-and-page-number
  (testing "Paging test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          count-of-items 200
          records (for [first-name (gen-string 5 "John")
                        middle-name (gen-string 5 "Alex")
                        last-name (gen-string 5 "Smith")
                        address (gen-string 5 "Utopia")
                        sex (gen-from-set 5 (into #{} (filter #(not= "unknown" %) sp/sex-enum)))]
                    {:first-name first-name
                     :middle-name middle-name
                     :last-name last-name
                     :address address
                     :sex sex
                     :oms (tg/oms-gen)})
          _ (doseq [r (take count-of-items records)] (service/add-patient-info r))]
      (is (= (:total (service/get-all {:filters {}})) count-of-items))
      (is (= (-> (:data (service/get-all {:filters {} :paging {:page-size 50 :page-number 1}})) (count)) 50))
      (loop [page 1 ids #{}]
        (let [fetched-ids
              (into #{} (mapv :id
                              (:data (service/get-all {:filters {} :paging {:page-size 33 :page-number page}}))))]
          (when-not (empty? fetched-ids)
            (is (empty? (clojure.set/intersection ids fetched-ids)))
            (recur (inc page) (clojure.set/join ids fetched-ids))))))))

(deftest fetch-patient-info-with-ordering
  (testing "Ordering test"
    (let [{:keys [data total]} (service/get-all {:filters {}})
          _ (is (and (empty? data) (zero? total)))
          oms-values (->> (range 10) (map (fn [_] (tg/oms-gen))))
          _ (doseq [o oms-values] (service/add-patient-info {:oms o}))
          id-desc (mapv :id (:data (service/get-all {:filters {} :sorting {:id :desc}})))
          id-asc (mapv :id (:data (service/get-all {:filters {} :sorting {:id :asc}})))]
      (is (< (first id-asc) (first (reverse id-asc))))
      (is (> (first id-desc) (first (reverse id-desc)))))))
