# config-reader

[`config-reader`](https://github.com/vial0ft/ouch-it-hurts/tree/main/src/clj/ouch_it_hurts/config_reader) is utility for reading `edn` configuration file in `resources` of a project. Reader is able to resolve system **environment variable** by name and. If it isn't exist reader would use `:default` when `:default` key defines.

## Example

Configuration file `resource/config.edn`:

```clojure
 :server/http {:port {:env "PORT" :default "3000"}}
 ...
 :db/connection {:dbtype "postgresql"
                 :username {:env "DB_USER" :default "postgres"}
                 :password {:env "DB_PASSWORD"}
                 :host {:env "DB_HOST" :default "localhost"}
                 :port {:env "DB_PORT" :deafult "5432"}
                 :dbname "postgres"
                 :dataSourceProperties {:socketTimeout 30}
                 :maximumPoolSize 30}
```

###  Getting raw data:

```clojure
(config-reader.core/read-config "config.edn")
;; or if if you wanna get some keys
(config-reader.core/read-config "config.edn" [:k1 :k2])
```

### Get and resolve environment variables:

```clojure
(-> (config-reader.core/load-config "config.edn")
    (config-reader.core/resolve-props)
```
