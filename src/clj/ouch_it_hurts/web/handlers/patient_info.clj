(ns ouch-it-hurts.web.handlers.patient-info
  (:require
   [reitit.ring :as ring]
   [ring.util.http-response :as http-response]
   [reitit.ring.middleware.exception :as exception]
   [ouch-it-hurts.patients-info.service :as s]
   [ouch-it-hurts.web.handlers.mappers.core :as m]
   [ouch-it-hurts.specs :as specs]))

(defn- result
  [action {:keys [then otherwise fallback] :or {otherwise (fn [_] nil) fallback (fn[e] (throw e))}}]
   (try
     (let [[result-key result] (action)]
       (case result-key
         :ok (then result)
         (otherwise result)))
     (catch Throwable e (fallback {:ex e}))))

(defn get-all
  ([{:keys [body]}]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/query-request body)
    {:then (fn [ok-query-params]
             (-> (s/get-all ok-query-params)
                 (update-in [:data] #(when-let [data %] (map m/patient-info-serializer data)))
                 (http-response/ok)))
     :otherwise (fn [err] (http-response/bad-request {:error err}))
     :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{:keys [body] :as req} respond raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/query-request body)
    {:then (fn [ok-query-params]
             (-> (s/get-all ok-query-params)
                 (update-in [:data] #(when-let [data %] (map m/patient-info-serializer data)))
                 (http-response/ok)
                 (respond)))
     :otherwise (fn [err] (respond (http-response/bad-request {:error err})))
     :fallback (fn [err] (respond (http-response/bad-request {:error (:ex err)})))})))

(defn add-new
  ([{:keys [body]}]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/add-patient-form body)
    {:then  (fn [b]
              (->> (m/patient-info-deserializer b)
                   (s/add-patient-info)
                   (m/patient-info-serializer)
                   (http-response/created nil)))
     :otherwise (fn [err] (http-response/bad-request {:error err}))
     :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{:keys [body]} responde raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/add-patient-form body)
    {:then (fn [b]
             (->> (m/patient-info-deserializer b)
                  (s/add-patient-info)
                  (m/patient-info-serializer)
                  (http-response/created nil)
                  (responde)))
    :otherwise (fn [err] (responde (http-response/bad-request {:error err})))
    :fallback (fn [err] (responde (http-response/bad-request {:error (:ex err)})))})))

(defn get-by-id
  ([{{:keys [id]} :path-params}]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
    {:then  #(-> (s/get-by-id %)
                 (m/patient-info-serializer)
                 (http-response/ok))
     :otherwise (fn [err] (http-response/bad-request {:error err}))
     :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{{:keys [id]} :path-params} responde raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
    {:then  #(-> (s/get-by-id %)
                 (m/patient-info-serializer)
                 (http-response/ok)
                 (responde))
     :otherwise (fn [err] (responde (http-response/bad-request {:error err})))
     :fallback (fn [err] (responde (http-response/bad-request {:error (:ex err)})))})))

(defn update-info
  ([{existed-patient :body {:keys [id]} :path-params}]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/patient-info existed-patient)
    {:then #(let [info (m/patient-info-deserializer %)]
              (-> (s/update-patient-info (:id info) info)
                  (m/patient-info-serializer)
                  (http-response/ok)))
     :otherwise (fn [err] (http-response/bad-request {:error err}))
     :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{existed-patient :body {:keys [id]} :path-params} responde raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/patient-info existed-patient)
    {:then #(let [info (m/patient-info-deserializer %)]
              (-> (s/update-patient-info (:id info) info)
                  (m/patient-info-serializer)
                  (http-response/ok)
                  (responde)))
     :otherwise (fn [err] (responde (http-response/bad-request {:error err})))
     :fallback (fn [err] (responde (http-response/bad-request {:error (:ex err)})))})))

(defn delete
  ([{{:keys [id]} :path-params}]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
    {:then #(-> (s/delete-patient-info %)
                (m/patient-info-serializer)
                (http-response/ok))
     :otherwise (fn [err] (http-response/bad-request {:error err}))
     :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{{:keys [id]} :path-params} responde raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
    {:then #(-> (s/delete-patient-info %)
                (m/patient-info-serializer)
                (http-response/ok)
                (responde))
     :otherwise (fn [err] (responde (http-response/bad-request {:error err})))
     :fallback (fn [err] (responde (http-response/bad-request {:error (:ex err)})))})))

(defn restore-by-id
  ([{{:keys [id]} :path-params}]
  (result
   #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
   {:then #(-> (s/restore-patient-info %)
               (m/patient-info-serializer)
               (http-response/ok))
    :otherwise (fn [err] (http-response/bad-request {:error err}))
    :fallback  (fn [ex] (http-response/bad-request {:error (:ex ex)}))}))

  ([{{:keys [id]} :path-params} responde raise]
   (result
    #(specs/confirm-if-valid :ouch-it-hurts.specs/id id)
    {:then #(-> (s/restore-patient-info %)
                (m/patient-info-serializer)
                (http-response/ok)
                (responde))
     :otherwise (fn [err] (responde (http-response/bad-request {:error err})))
     :fallback (fn [err] (responde (http-response/bad-request {:error (:ex err)})))})))


(defn not-found
  ([request]
   (println "sync")
   (println request)
   {:status 404
    :headers {"Content-Type" "text/plain"}
    :body "oops"})
  ([request respond raise]
   (println "async")
   (println request)
   (respond {:status 404
             :headers {"Content-Type" "text/plain"}
             :body "oops"})))

(def routes
  (ring/ring-handler
   (ring/router
    [
     ["/patients" {:get {:handler get-all}}]
     ["/patient" {:post {:handler add-new}}]
     ["/patient/:id" {:get {:handler get-by-id}
                      :put {:handler update-info}
                      :delete {:handler delete}}]
     ["/patient/:id/restore" {:post {:handler restore-by-id}}]]
    {:data {:middleware [exception/exception-middleware]}})
   (ring/create-default-handler
    {:not-found not-found})))
