(ns ouch-it-hurts.specs
  (:require #?(:clj [clojure.spec.alpha :as s] :cljs [cljs.spec.alpha :as s])
            #?(:cljs [goog.string :as gstr])
            #?(:cljs goog.string.format)
            [spec-tools.core :as st]
            [spec-tools.data-spec :as ds]
            [clojure.string :as string]))

(def str-format #?(:clj format :cljs (or gstr/format goog.string.format)))

(def oms-numbers-count 16)
(def sex-enum #{"male" "female" "unknown"})
(def order-enum #{:asc :desc})

(def show-records-opts #{:not-deleted-only :deleted-only :all})

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
    :description (str-format "'Id' value must be positive integer number")}))

(s/def ::sex
  (st/spec
   {:spec (s/nilable (s/and string? #(contains? sex-enum %)))
    :description (str-format "'Sex' value must be one of %s" (str sex-enum))}))

(s/def ::first-name
  (st/spec
   {:spec (s/nilable (s/and string? #(< (count %) 255)))
    :description (str-format "'First name' value limited by %s letters" 255)}))

(s/def ::middle-name
  (st/spec
   {:spec (s/nilable (s/and string? #(< (count %) 255)))
    :description (str-format "'Middle name' value limited by %s letters" 255)}))

(s/def ::last-name
  (st/spec
   {:spec (s/nilable (s/and string? #(< (count %) 255)))
    :description (str-format "'Last name' value limited by %s letters" 255)}))


(s/def ::address
  (st/spec
   {:spec (s/nilable (s/and string? #(< (count %) 255)))
    :description (str-format "'Address' value limited by %s letters" 255)}))

(s/def ::oms
  (st/spec
   {:spec (s/nilable (s/and string? valid-oms?))
    :description (str-format "'CMI' value must fit the format '%s'" (string/join (repeat oms-numbers-count "0")))}))

(s/def ::maybe-date-YYYY-MM-DD
  (st/spec
   {:spec (s/nilable (s/and string? valid-date-format?))
    :description (str-format "Date value must be valid date and fit format %s" "YYYY-MM-DD")}))

(s/def ::birth-date
  (merge
   (s/spec ::maybe-date-YYYY-MM-DD)
   {:description (str-format "'Birth date' value must be valid date and fit the format %s" "YYYY-MM-DD")}))

(s/def ::deleted boolean?)

(s/def ::new-patient-info
  (st/spec
   {:spec (s/and map? #(not-empty %) (s/keys :opt-un  [::first-name
                                                       ::last-name
                                                       ::middle-name
                                                       ::sex
                                                       ::address
                                                       ::birth-date
                                                       ::oms]))
    :description
    (str-format (string/join " " ["'Patient info' must contain at least one of fields:"
                                  "'First name'" "'Last name'" "'Middle name'" "'Address'" "'Sex'" "'Birth date'" "'CMI'"]))}))

(s/def ::patient-info
  (st/spec
   {:spec (s/and ::new-patient-info (s/keys :req-un [::id ::deleted]))
    :desciption "'Patient info' of an existed patient must contain 'id' and 'deleted'"}))

;;;;;;;;;;;;;;;;;;;;;;
;;
;; Request & Responses
;;
;;;;;;;;;;;;;;;;;;;;;;

(s/def ::birth-date-period
  (st/spec
   {:spec (s/and map? #(not-empty %) (s/map-of #{:from :to} ::maybe-date-YYYY-MM-DD))
    :description "Birth date period must contain 'from' or 'to' or both values formatted by 'YYYY-MM-DD'"}))

(s/def ::sex-opts
  (st/spec
   {:spec (s/or :many (s/and coll? not-empty (s/coll-of ::sex :into #{})) :one ::sex)
    :description (str-format "'Sex' options must contain at least one of: %s" (str sex-enum))}))

(s/def ::show-records-opts
  (st/spec
   {:spec (s/and keyword? #(contains? show-records-opts %))}))

(s/def ::filters
  (s/keys :opt-un [::first-name
                   ::last-name
                   ::middle-name
                   ::address
                   ::oms
                   ::birth-date-period
                   ::sex-opts
                   ::show-records-opts]))

(s/def ::page-number
  (st/spec
   {:spec (s/and pos-int?)
    :description "'Page number' must be positive integer"}))

(s/def ::page-size
  (st/spec
   {:spec (s/and pos-int?)
    :description (str-format "'Page size' must be positive integer and limited by %s" page-size-limit)}))

(s/def ::paging
  (st/spec
   {:spec (s/keys :req-un [::page-number ::page-size])
    :description "Paging must contain 'page-number' and 'page-size'"}))

(s/def ::order
  (st/spec
   {:spec (s/and keyword? order-enum)
    :description "'Order' value must be one of 'asc' or 'desc'"}))

(s/def ::sorting
  (st/spec
   {:spec (s/map-of #{:id
                      :first-name
                      :last-name
                      :middle-name
                      :sex
                      :address
                      :birth-date
                      :oms} ::order)
    :description (string/join " " ["'Sorting' map must contain at least one of keys:"
                                   "'id'" "'First name'" "'Last name'" "'Middle name'" "'Address'" "'Sex'" "'Birth date'" "'CMI'"
                                   "- with 'order' value"])}))

(s/def ::query-request
  (st/spec
   {:spec (s/and map? (s/keys :opt-un [::filters ::sorting]) (s/keys :req-un [::paging]))
    :description "Request of patients records by filters, sorting and paging. Paging is require. Filters and sorting optional"}))

(s/def ::data
  (st/spec
   {:spec (s/coll-of ::patient-info)
    :desciption "Collection of patients records according filter"}))

(s/def ::total
  (st/spec
   {:spec pos-int?
    :desciption "Total number of record according filter must be positive integer"}))

(s/def ::query-response
  (st/spec
   {:spec (s/keys :req-un [::data ::total])
    :description ""}))

(s/def ::add-patient-form ::new-patient-info)
(s/def ::add-patient-response ::patient-info)

(s/def ::edit-patient-form ::patient-info)
(s/def ::edit-patient-response ::patient-info)

(s/def ::delete-patient-request ::id)
(s/def ::delete-patient-response ::id)

(def xform
  (comp
   (filter (fn [[k v]] (= (keyword (name k)) :problems)))
   (mapcat (fn [[k v]] v))
   (map (fn [{:keys [via]}] (last via)))
   (map (fn [problem-spec] [(:description (s/spec problem-spec))]))))

(defn validation-messages [explain-data]
  (transduce xform into #{} explain-data))

(defn confirm-if-valid [spec data]
  (let [result (st/coerce spec data st/string-transformer)]
    (if (s/valid? spec result) [:ok result]
        [:error (validation-messages (s/explain-data spec result))])))
