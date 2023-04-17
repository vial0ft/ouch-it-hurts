(ns ouch-it-hurts.specs
  (:require [clojure.spec.alpha :as s]
            [spec-tools.core :as st]
            [spec-tools.data-spec :as ds]
            [clojure.string :as string]))


(def str-format #?(:clj format :cljs goog.string/format))

(def parse-id-number #?(:clj bigint :cljs js/parseInt))



(defmacro ^:private one-or-more-keys [ks]
  (let [keyset (set (map (comp keyword name) ks))]
    `(s/and (s/keys :opt-un ~ks)
            #(some ~keyset (keys %)))))

(def oms-numbers-count 16)
(def sex-enum #{:male :female :other :unknown})
(def order-enum #{:asc :desc})

(def page-size-limit 100)

(def oms-pattern (re-pattern (str-format "^\\d{%d}$" oms-numbers-count)))

(defn valid-oms? [str] (some? (re-matches oms-pattern str)))

(defn valid-date-format? [date-like-str]
  (some?
   (re-matches
    #"(?:19\d{2}|20[01][0-9]|202[0-3])[-/.](?:0[1-9]|1[012])[-/.](?:0[1-9]|[12][0-9]|3[01])"
    date-like-str)))

(s/def ::id
  (st/spec
   {:spec (s/and number? pos?)
    :description (str-format "'Id' value must be positive integer number")
    :name ::id
    :json-schema/type {:type "positiveInteger", :format "bigint"}
    :json-schema/example "42"
    :decode/string #(-> %2 parse-id-number)
    :encode/string #(.toString %2)})
  )

(def sex-enum? (s/and keyword? sex-enum))

(s/def ::sex
  (st/spec
   {:spec sex-enum?
    :description (str-format "'Sex' value must be one of %s" (str sex-enum))
    :json-schema/type {:type "string", :format "keyword"}
    :json-schema/example "male"
    :decode/string #(-> %2 name string/lower-case keyword)
    :encode/string #(-> %2 name string/lower-case)}))

(s/def ::first-name
  (st/spec
   {:spec (s/and string? #(< (count %) 255))
    :description (str-format "'First name' value limited by %s letters" 255)
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "John"})
  )


(s/def ::second-name
  (st/spec
   {:spec (s/and string? #(< (count %) 255))
    :description (str-format "'Second name' value limited by %s letters" 255)
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "Dou"})
  )


(s/def ::middle-name
  (st/spec
   {:spec (s/and string? #(< (count %) 255))
    :description (str-format "'Middle name' value limited by %s letters" 255)
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "Joe"})
  )

(s/def ::address
  (st/spec
   {:spec (s/and string? #(< (count %) 255))
    :description (str-format "'Address' value limited by %s letters" 255)
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "Joe"})
  )

(s/def ::oms
  (st/spec
   {:spec (s/and string? valid-oms?)
    :description (str-format "'CMI' value must fit the format '%s'" (string/join (repeat oms-numbers-count "0")))
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "Joe"})
  )


(s/def ::date-YYYY-MM-DD
  (st/spec
   {:spec (s/and string? valid-date-format?)
    :description (str-format "Date value must be valid date and fit format %s" "YYYY-MM-DD")
    :json-schema/type {:type "string", :format "string"}
    :json-schema/example "2021-01-01"})
  )

(s/def ::birth-date
  (merge
   (s/spec ::date-YYYY-MM-DD)
   {:description (str-format "'Birth date' value must be valid date and fit the format %s" "YYYY-MM-DD")}
   )
  )

(s/def ::new-patient-info
  (st/spec
   {:spec (s/and map? #(not-empty %) (s/keys :opt-un  [::first-name
                                                       ::second-name
                                                       ::middle-name
                                                       ::sex
                                                       ::address
                                                       ::birth-date
                                                       ::oms]))
    :description
    (str-format (string/join " " ["'Patient info' must contain at least one of fields:"
                             "'First name'" "'Second name'" "'Middle name'" "'Address'" "'Sex'" "'Birth date'" "'CMI'"]))
    })
  )

(s/def ::patient-info
  (st/spec
   {:spec (s/and ::new-patient-info (s/keys :req-un [::id]))
    :desciption "'Patient info' of an existed patient must contain 'id'"
    })
  )

;;;;;;;;;;;;;;;;;;;;;;
;;
;; Request & Responses
;;
;;;;;;;;;;;;;;;;;;;;;;


(s/def ::date-period
  (st/spec
   {:spec (s/and map? #(not-empty %) (s/map-of #{:from :to} ::date-YYYY-MM-DD))
    :description "Date period must contain 'from' or 'to' values or both"
    })
  )


(s/def :filters/sex
  (st/spec
   {:spec (s/and coll? #(not-empty %) (s/coll-of ::sex :into #{}))
    :description (str-format "Filter by 'Sex' must contain at least one of: %s" (str sex-enum))
    }
    ))

(s/def :filters/birth-date ::date-period)
(s/def :filters/oms ::oms)
(s/def :filters/first-name ::first-name)
(s/def :filters/second-name ::second-name)
(s/def :filters/middle-name ::middle-name)
(s/def :filters/address ::address)

(s/def :paging/page-number
  (st/spec
   {:spec (s/and pos-int?)
    :description "'Page number' must be positive integer"
    })
  )

(s/def :paging/page-size
  (st/spec
   {:spec (s/and pos-int?)
    :description (str-format "'Page size' must be positive integer and limited by %s" page-size-limit)
    })
  )

(s/def ::order
  (st/spec
   {:spec (s/and keyword? order-enum)
    :description "'Order' value must be one of 'asc' or 'desc'"
    })
  )

(s/def :query-request/sorting
  (st/spec
  {:spec (s/map-of #{:id
                     :first-name
                     :second-name
                     :middle-name
                     :sex
                     :address
                     :birth-date
                     :oms} ::order)
   :description (string/join " " ["'Sorting' map must contain at least one of keys:"
                     "'id'" "'First name'" "'Second name'" "'Middle name'" "'Address'" "'Sex'" "'Birth date'" "'CMI'"
                     "- with 'order' value"])
   })
  )





(s/def :query-request/paging (s/keys :req-un [:paging/page-number :paging/page-size]))

(s/def :query-request/filters
  (one-or-more-keys [:filters/first-name
                     :filters/second-name
                     :filters/middle-name
                     :filters/sex
                     :filters/address
                     :filters/birth-date
                     :filters/oms]))


(s/def ::query-request
   (st/spec
    {:spec (s/and map? (s/keys :opt-un [:query-request/filters :query-request/sorting]) (s/keys :req-un [:query-request/paging]))
     :desciption "Request of patients records by filters, sorting and paging. Paging is require. Filters and sorting optional"
     })
  )

(s/def :query-response/data
  (st/spec
   {
    :spec (s/coll-of ::patient-info)
    :desciption "Collection of patients records according filter"
    })
  )

(s/def :query-response/total
  (st/spec
   {:spec pos-int?
    :desciption "Total number of record according filter must be positive integer"
    })
  )

(s/def ::query-response
  (st/spec
   {:spec (s/keys :req-un [:query-response/data :query-response/total])
    :description ""
    })
  )


(s/def ::add-patient-request ::new-patient-info)
(s/def ::add-patient-response ::patient-info)

(s/def ::edit-patient-request ::patient-info)
(s/def ::edit-patient-response ::patient-info)

(s/def ::delete-patient-request ::id)
(s/def ::delete-patient-response ::id)

