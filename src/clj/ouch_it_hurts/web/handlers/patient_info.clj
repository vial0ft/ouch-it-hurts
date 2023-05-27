(ns ouch-it-hurts.web.handlers.patient-info
  (:require
   [ouch-it-hurts.patients-info.service :as s]
   [ouch-it-hurts.web.handlers.mappers.core :as m]
   [ouch-it-hurts.web.http-responses.core :as http-resp]
   [ouch-it-hurts.specs :as specs]))

(defn- result
  ([action ok-action] (result action ok-action nil))
  ([action ok-action fail-action]
   (let [[result-key result] (action)]
     (case result-key
       :ok (ok-action result)
       (fail-action result)))))

(defn- get-all
  [{{:keys [query-params]} :app/request}]
  (println (str query-params))
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/query-request query-params)
   (fn [ok-query-params]
     (-> (s/get-all ok-query-params)
         (update-in [:data] #(when-let [data %] (map m/patient-info-serializer data)))
         (http-resp/json-ok)))
   #(http-resp/json-bad-request {:error %})))

(defn- add-new [{{:keys [body]} :app/request}]
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/add-patient-form body)
   #(-> (m/patient-info-deserializer %) (s/add-patient-info) (http-resp/json-ok))
   #(http-resp/json-bad-request {:error %})))

(defn- get-by-id [{{{:keys [id]} :path-params} :app/request}]
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
   #(-> (s/get-by-id %) (m/patient-info-serializer) (http-resp/json-ok))
   #(http-resp/json-bad-request {:error %})))

(defn- update-info [{{existed-patient :body {:keys [id]} :path-params} :app/request}]
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/patient-info existed-patient)
   #(let [info (m/patient-info-deserializer %)]
      (-> (s/update-patient-info (:id info)  info) (http-resp/json-ok)))
   #(http-resp/json-bad-request {:error %})))

(defn- delete [{{{:keys [id]} :path-params} :app/request}]
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
   #(-> (s/delete-patient-info %) (http-resp/json-ok))
   #(http-resp/json-bad-request {:error %})))

(defn routes []
  [["/patients" {:get {:handler get-all}}]
   ["/patient" {:post {:handler add-new}}]
   ["/patient/:id" {:get {:handler get-by-id}
                    :put {:handler update-info}
                    :delete {:handler delete}}]])
