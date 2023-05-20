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
   ["-s" "--server SERVER_PATH" "Hostname of patients service"
    :parse-fn str]]
  )

(defn read-file [file-name]
  (with-open [rdr (io/reader file-name)]
    (slurp rdr)))


(defn write [edn-config file-name]
  (spit file-name edn-config))


(defn update-config [edn-config server-path]
  (assoc edn-config :closure-defines {'ouch-it-hurts.api.server-path server-path}))


(let [{:keys[template out server]} (:options (parse-opts *command-line-args* patients-service-opts))]
  (printf "template= %s out= %s server= %s" template out server)
  (if (not-every? some? [template out server]) "something missed"
      (-> (read-file template)
          (edn/read-string)
          (update-config server)
          (write out))
      ))
