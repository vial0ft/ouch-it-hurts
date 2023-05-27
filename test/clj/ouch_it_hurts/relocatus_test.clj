(ns ouch-it-hurts.relocatus-test
  (:require
   [ouch-it-hurts.relocatus.core :as relocat]
   [ouch-it-hurts.relocatus.repo :as relr]
   [ouch-it-hurts.relocatus.helpers :as h]
   [ouch-it-hurts.config-reader.core :as cr]
   [ouch-it-hurts.helpers.core :as th]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]))

(def ^:dynamic *cfg* nil)
(def ^:dynamic *ds*)

(defn- write-to-file [file text]
  (with-open [w (io/writer (io/as-file (io/resource file)))]
    (.write w text)))

(defn- columns [ds schema table]
  (->> (jdbc/execute! ds ["select column_name FROM information_schema.columns
                                           where table_schema = ? and table_name = ?"
                          schema
                          table])
       (mapv :columns/column_name)))

(defn- clean-directory [directory-path]
  (let [files-to-delete (filter #(s/ends-with? % ".sql") (file-seq directory-path))]
    (doseq [file files-to-delete]
      (io/delete-file (.getPath file)))))

(defn- once [work]
  (let [reloc-cfg (-> (cr/read-config "config.edn" [:relocatus/migrations])
                   (cr/resolve-props)
                   (get :relocatus/migrations))
        container (-> (th/create-container (select-keys (:db reloc-cfg) [:dbname :user :password]))
                      (th/start-container))
        cfg       (-> reloc-cfg
                      (assoc-in [:db :host] (:host container))
                      (assoc-in [:db :port] (get (:mapped-ports container) 5432)))
        datasource (jdbc/get-datasource (:db cfg))]
    (binding [*cfg* cfg
              *ds*  datasource]
      (work))
    (th/stop-container container)))

(defn- each [work]
  (work)
  (-> (:migration-dir *cfg*)
      (io/resource)
      (io/as-file)
      (clean-directory)))

(use-fixtures :once once)
(use-fixtures :each each)

(deftest creating-migrations-test
  (testing "Create migrations"
    (let [migration-name "test-migration"
          _ (relocat/create-migration *cfg* migration-name)
          {:keys [migration-dir]} *cfg*
          {:keys [up down]} (h/up-down-migration-scripts migration-dir migration-name)]
      (is (not (nil? up)))
      (is (not (nil? down)))
      (is (s/ends-with? up ".up.sql"))
      (is (s/ends-with? down ".down.sql")))))

(deftest init-migration-table-test
  (testing "Init relocatus table"
    (let [_ (relocat/init-migration-table *cfg*)
          migration-table-requied-columns (columns *ds*
                                                   (get-in *cfg* [:migration-db :schema])
                                                   (get-in *cfg* [:migration-db :table]))]
      (is (some #(= % "hash") migration-table-requied-columns))
      (is (some #(= % "migration_name") migration-table-requied-columns))
      (jdbc/execute! *ds* [(format "DROP SCHEMA IF EXISTS %s CASCADE;" (get-in *cfg* [:migration-db :schema]))]))))

(deftest applying-one-migration-test
    (testing "Applying & rollback one migration"
      (let [_ (relocat/init-migration-table *cfg*)
            migration-name "test-migration"
            {:keys [migration-dir]} *cfg*
            _ (relocat/create-migration *cfg* migration-name)
            {:keys [up down]} (h/up-down-migration-scripts migration-dir migration-name)
            _ (write-to-file up "create table if not exists public.test_table (
                                  ID INT PRIMARY KEY      NOT NULL,
                                  DEPT           CHAR(50) NOT NULL,
                                  EMP_ID         INT      NOT NULL);")
            _ (write-to-file down "drop table if exists test_table;")
            _ (relocat/migrate *cfg*)
            migrations (relr/get-migrations *ds* (h/schema-table (:migration-db *cfg*)))
            test-table-columns (columns *ds* "public" "test_table")]
        (is (= migration-name (some #(when-let [name (:migration-name %)] name) migrations)))
        (is (= (into #{} (map s/upper-case test-table-columns)) #{"ID" "DEPT" "EMP_ID"}))
        (relocat/rollback *cfg*)
        (is (empty? (columns *ds* "public" "test_table")))
        (is (empty? (relr/get-migrations *ds* (h/schema-table (:migration-db *cfg*)))))
        (jdbc/execute! *ds* [(format "DROP SCHEMA IF EXISTS %s CASCADE;" (get-in *cfg* [:migration-db :schema]))]))))

(deftest applying-few-migrations-test
    (testing "Applying & rollback few migrations"
      (let [_ (relocat/init-migration-table *cfg*)
            {:keys [migration-dir]} *cfg*
            migrations [{:name "create-test-table"
                         :up "create table if not exists public.test_table (
                                  ID INT PRIMARY KEY      NOT NULL,
                                  DEPT           CHAR(50) NOT NULL,
                                  EMP_ID         INT      NOT NULL);"
                         :down "drop table if exists test_table;"}
                        {:name "add-field-test-table"
                         :up "ALTER TABLE public.test_table ADD COLUMN IF NOT EXISTS age INTEGER;"
                         :down "ALTER TABLE public.test_table DROP COLUMN IF EXISTS age;"}]
            _ (doseq [migration migrations]
                (let [_ (relocat/create-migration *cfg* (:name migration))
                      {:keys [up down]} (h/up-down-migration-scripts migration-dir (:name migration))]
                  (write-to-file up (:up migration))
                  (write-to-file down (:down migration))))
            _ (relocat/migrate *cfg*)
            applied-migrations (relr/get-migrations *ds* (h/schema-table (:migration-db *cfg*)))
            test-table-columns (columns *ds* "public" "test_table")]
        (is (= (into #{} (map :migration-name applied-migrations))
               (into #{} (map :name migrations))))
        (is (= (into #{} (map s/upper-case test-table-columns)) #{"ID" "DEPT" "EMP_ID" "AGE"}))
        (relocat/rollback *cfg*)
        (relocat/rollback *cfg*)
        (is (empty? (columns *ds* "public" "test_table")))
        (is (empty? (relr/get-migrations *ds* (h/schema-table (:migration-db *cfg*)))))
        (jdbc/execute! *ds* [(format "DROP SCHEMA IF EXISTS %s CASCADE;" (get-in *cfg* [:migration-db :schema]))]))))
