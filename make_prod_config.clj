(require '[clojure.java.io :as io]
         '[clojure.edn :as edn]
         '[clojure.string :refer [ends-with?]]
         '[clojure.tools.cli :refer [parse-opts]])


(def patients-service-opts
  [["-t" "--template TEMPLATE_FILE" "Template of final configuration"
    :parse-fn str
    :validate [#(ends-with? % ".edn") "Must be `edn`-file"]
    ]

   ["-o" "--out EDN_FILE" "File of configuration"
    :parse-fn str
    :validate [#(ends-with? % ".edn") "Must be `edn`-file"]
    ]
   ["-p" "--port PORT" "Port of the patients service"
    :parse-fn #(Integer/parseInt %)
    :default 80
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-H" "--host HOST" "Hostname of patients service"
    :parse-fn str]]
  )

(defn read-file [file-name]
  (with-open [rdr (io/reader file-name)]
    (slurp rdr)))


(defn write [edn-config file-name]
  (spit file-name edn-config))


(defn update-config [edn-config host port]
  (assoc edn-config :closure-defines {'ouch-it-hurts.api.host host
                                      'ouch-it-hurts.api.port port}))


(let [{:keys[template out port host]} (:options (parse-opts *command-line-args* patients-service-opts))]
  (printf "template= %s out= %s host= %s port= %s" template out host port)
  (if (not-every? some? [template out port host]) "something missed"
      (-> (read-file template)
          (edn/read-string)
          (update-config host port)
          (write out))
      ))
