(ns ouch-it-hurts.relocatus.repo
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.sql :as sql]))


(defn- create-migration-table-sql [schema tablename]
  (format
   "create schema if not exists %s;
    create table if not exists %s (
           id serial4 PRIMARY KEY,
           migration_name text NOT NULL,
           hash BIGINT NOT NULL,
           create_at timestamp NOT NULL DEFAULT now(),
           UNIQUE(hash),
           UNIQUE(migration_name)
    );" schema (str schema "." tablename)))

(defn- insert-migration-sql [tablename] (format "insert into %s (migration_name, hash) values(?,?)" tablename))

(defn- delete-migration-sql [tablename] (format "delete from %s where migration_name = ?" tablename))

(defn- insert-migration-record [ds table {:keys [migration-name hash]}]
  (jdbc/execute! ds [(insert-migration-sql table) migration-name hash]))

(defn- apply-migration [ds script] (jdbc/execute! ds [script]))

(defn- delete-migration [ds table name] (jdbc/execute! ds [(delete-migration-sql table) name]))

(defn get-migrations [ds table]
  (jdbc/execute! ds [(format "select id, migration_name, hash from %s" table)]
                 {:builder-fn rs/as-unqualified-kebab-maps}))

(defn create-migration-table [ds {:keys [schema table]}]
  (jdbc/execute! ds [(create-migration-table-sql schema table)]))

(defn do-migration [ds table {:keys [migration-name hash] :as migration-info} up-script]
  (jdbc/with-transaction [tx ds]
    (try
      (apply-migration tx up-script)
      (insert-migration-record tx table migration-info)
      {:ok migration-name}
      (catch Exception e
        (do (.rollback tx)
            (throw (ex-info "Error during applying migration"
                            {:scenario up-script :migration migration-name}
                            e)))))))

(defn do-rollback [ds table migration-name down-script]
  (jdbc/with-transaction [tx ds]
    (try
      (apply-migration tx down-script)
      (delete-migration tx table migration-name)
      (catch Exception e
        (do (.rollback tx)
            (throw (ex-info "Error during rollback of migration"
                            {:migration migration-name :scenario down-script}
                            e)))))))


