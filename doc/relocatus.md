# relocatus

`relocatus` is a utility that runs and rollbacks sql migrations on database.

## config

`relocatus` require of configuration place for migrations and db-connection config:

```clojure
 :relocatus/migrations {
                        :migration-dir  "migrations"
                        :migration-db {:schema "relocatus" :table "relocatus"}
                        :db {
                             :dbtype "postgresql"
                             :dbname "postgres"
                             :classname "org.postgresql.Driver"
                             :host {:env "DB_HOST"}
                             :port {:env "DB_PORT"}
                             :user {:env "DB_USER"}
                             :password {:env "DB_PASSWORD"}
                             }
                        }
```
## create migrations

Run in repl or code:

```clojure
(require '[relocatus.core :as relocatus])

;; read config file to `cfg` or provide `{:migration-dir "dir-for-migrations"}` map

(relocatus/create-migration cfg "migration-name")
```
After that you will find two migrations files in `:migration-dir` with names `yyyyMMddHHmmssSSS_migration-name.up.sql` and `yyyyMMddHHmmssSSS_migration-name.down.sql`. `*.up.sql` - file for the migration script and `*.down.sql` - for rollback of migration. `yyyyMMddHHmmssSSS` prefix needs for ordering of migrations.


## init table for info about migrations

Run in repl or code:

```clojure
(require '[relocatus.core :as relocatus])

;; read config file to `cfg` of provide info about database connection and `relocatus` table

(relocatus/init-migration-table cfg)
```
It creates a table for records about migrations had been applied

## apply migrations


Run in repl or code:

```clojure
(require '[relocatus.core :as relocatus])

;; read config file to `cfg`

(relocatus/migrate cfg)

```

It applies all `*.up.sql`  migrations one by one according `yyyyMMddHHmmssSSS` prefix and makes record in `relocatus-table` that migration was applied. If some of transactions have already applied - they will be skipped.



## rollback migrations

Run in repl or code:

```clojure
(require '[relocatus.core :as relocatus])

;; read config file to `cfg`

(relocatus/rollback cfg)

```

It rollbacks last transaction with `*.down.sql` script.


## !Attention!

DON'T MODIFY EXISTING SCRIPTS - CREATE NEW
